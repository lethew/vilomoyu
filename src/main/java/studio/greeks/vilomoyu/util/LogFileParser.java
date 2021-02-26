package studio.greeks.vilomoyu.util;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.h2.util.StringUtils;
import org.slf4j.event.Level;
import studio.greeks.vilomoyu.model.entity.*;
import studio.greeks.vilomoyu.model.bo.ErrorStack;
import studio.greeks.vilomoyu.model.bo.LogRecord;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 基于正则表达式将日志信息文件（流）转换为数据库中存储的结构
 *
 * @author <a href="mailto:wuzhao-1@thunisoft.com>吴昭</a>
 */
@Slf4j
public class LogFileParser {
    private final List<LogRecord> records;
    private final List<LogItem> logItems = new LinkedList<>();
    private final List<ThrowableMessage> throwableMessages = new LinkedList<>();
    private final List<ThrowableStackTraceMapping> throwableStackTraceMappings = new LinkedList<>();
    private final Set<ThrowableClass> throwableClasses = new HashSet<>();
    private final Set<StackTrace> stackTraces = new HashSet<>();

    public LogFileParser(String path) throws FileNotFoundException {
        this(new FileInputStream(path));
    }

    public LogFileParser(InputStream inputStream) {
        records = new LinkedList<>();
        try (InputStreamReader reader = new InputStreamReader(inputStream);
             BufferedReader br = new BufferedReader(reader)) {
            foreachLine(br);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                // 清除当前线程中使用到的时间转换器
                FORMAT.remove();
                inputStream.close();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    public void reorganize(String systemId, String area, String tag) {
        for (LogRecord record : records) {
            LogItem logItem = LogItem.of(record);
            logItem.setId(UUIDUtil.getUuid());
            logItem.setArea(area);
            logItem.setSystemId(systemId);
            logItem.setImportTag(tag);
            logItems.add(logItem);
            reorganize(record.getStack(), logItem.getId());
        }
    }

    public List<LogItem> getLogItemList(){
        return logItems;
    }

    public List<ThrowableMessage> getThrowableMessageList() {
        return throwableMessages;
    }

    public List<ThrowableStackTraceMapping> getThrowableStackTraceMappingList() {
        return throwableStackTraceMappings;
    }

    public Set<StackTrace> getStackTraces(){
        return stackTraces;
    }

    public Set<ThrowableClass> getThrowableClassList() {
        return throwableClasses;
    }

    public List<LogRecord> getLogRecords() {
        return records;
    }

    private void reorganize(ErrorStack stack, String logId) {
        String pid = logId;
        int type = 1;
        while (stack != null) {

            String eClass = stack.getEClass();
            ThrowableClass throwableClass = new ThrowableClass(HashUtil.md5(eClass), eClass);
            throwableClasses.add(throwableClass);

            ThrowableMessage throwableMessage = ThrowableMessage.of(UUIDUtil.getUuid(), pid, type, throwableClass.getId(),StringUtil.substring(stack.getMessage(), 500), logId);
            throwableMessages.add(throwableMessage);

            int i = 0;
            for (StackTraceElement at : stack.getAts()) {
                StackTrace stackTrace = StackTrace.of(at);
                stackTraces.add(stackTrace);
                MappingKey mappingKey = new MappingKey(throwableMessage.getId(), stackTrace.getId());
                ThrowableStackTraceMapping mapping = ThrowableStackTraceMapping.of(mappingKey, i++);
                throwableStackTraceMappings.add(mapping);
            }
            if ((stack = stack.getNested()) != null) {
                pid = throwableMessage.getId();
                type = SUPPRESSED.equals(stack.getNestedType()) ? 2 : (CAUSED_BY.equals(stack.getNestedType()) ? 1 : -1);
            }
        }
    }

    private void foreachLine(BufferedReader br) {
        final AtomicReference<LogRecord> currentRecord = new AtomicReference<>();
        final AtomicReference<ErrorStack> currentStack = new AtomicReference<>();
        final AtomicBoolean lastIsLog = new AtomicBoolean(false);
        br.lines()
                .filter(Objects::nonNull)
                .map(String::trim)
                .forEach(line -> {
                    line = line.trim();
                    LogRecord record = currentRecord.get();
                    ErrorStack stack = currentStack.get();
                    LogRecord parseLog = parseLog(line);
                    if (null != parseLog) {
                        if (record != null) {
                            records.add(record);
                        }
                        currentRecord.set(parseLog);
                        lastIsLog.set(true);
                    } else if (record != null && isException(line)) {
                        newException(currentStack, lastIsLog, line, record);
                    } else if (record != null && isAt(line)) {
                        newStackTrace(lastIsLog, line, stack);
                    } else if (record != null && isCausedBy(line)) {
                        newSubException(currentStack, lastIsLog, line, stack, CAUSED_BY);
                    } else if (record != null && isSuppressed(line)) {
                        newSubException(currentStack, lastIsLog, line, stack, SUPPRESSED);
                    } else {
                        appendMessage(lastIsLog, line, record, stack);
                    }
                });
    }

    private void appendMessage(AtomicBoolean lastIsLog, String line, LogRecord record, ErrorStack stack) {
        if (record != null && !isCommonFrames(line)) {
            if (lastIsLog.get()) {
                record.setMessage(record.getMessage() + "\n" + line);
            } else {
                stack.setMessage(stack.getMessage() + "\n" + line);
            }
        }
    }

    private void newSubException(AtomicReference<ErrorStack> currentStack, AtomicBoolean lastIsLog, String line, ErrorStack stack, String caused) {
        stack.setNested(parseException(line));
        stack.setNestedType(caused);
        currentStack.set(stack.getNested());
        lastIsLog.set(false);
    }

    private void newStackTrace(AtomicBoolean lastIsLog, String line, ErrorStack stack) {
        if (stack.getAts() == null) {
            stack.setAts(new LinkedList<>());
        }
        StackTraceElement at = parseAt(line);
        stack.getAts().add(at);
        lastIsLog.set(false);
    }

    private void newException(AtomicReference<ErrorStack> currentStack, AtomicBoolean lastIsLog, String line, LogRecord record) {
        if (record.getStack() == null) {
            ErrorStack errorStack =  parseException(line);
            record.setStack(errorStack);
            currentStack.set(errorStack);
        }
        lastIsLog.set(false);
    }

    private static final String NATIVE_METHOD = "Native Method";
    private static final String SUPPRESSED = "Suppressed:";
    private static final String CAUSED_BY = "Caused by:";
    private static final ThreadLocal<SimpleDateFormat> FORMAT = ThreadLocal.withInitial(()->new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS"));
    private static Map<String, Level> LOG_LEVEL = new HashMap<>();
    static {
        for (Level value : Level.values()) {
            LOG_LEVEL.put(value.toString(), value);
        }
        LOG_LEVEL = Collections.unmodifiableMap(LOG_LEVEL);
    }
    private static final Pattern RECORD = Pattern.compile("^(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}:\\d{3}) \\[(\\S+)] \\[(\\S+)] \\[([\\w.]+):(\\d+)] - ([\\S ]+)$");
    private static final int LOG_TIMESTAMP_GROUP_INDEX = 1;
    private static final int LOG_LEVEL_GROUP_INDEX = 2;
    private static final int LOG_THREAD_GROUP_INDEX = 3;
    private static final int LOG_CLASS_GROUP_INDEX = 4;
    private static final int LOG_LINE_GROUP_INDEX = 5;
    private static final int LOG_MSG_GROUP_INDEX = 6;

    private static final Pattern EXCEPTION = Pattern.compile("^[a-z0-9A-Z\\\\.]+(Exception|Error|Throwable):");
    private static final Pattern COMMON_FRAMES = Pattern.compile("^... \\d+ common frames omitted$");

    private static final Pattern EXCEPTION_LINE = Pattern.compile("^(Caused by: |Suppressed: )?([\\w.]+(Exception|Error|Throwable)):([\\S ]*)$");
    private static final int EXCEPTION_CLASS_GROUP_INDEX = 2;
    private static final int EXCEPTION_MESSAGE_GROUP_INDEX = 4;

    private static final Pattern AT = Pattern.compile("^at ([$\\w.]+)\\.([\\w$]+)\\(([<>\\w ]+(\\.java)?)(:(\\d+))?\\)$");
    private static final int DECLARING_CLASS_GROUP_INDEX = 1;
    private static final int METHOD_NAME_GROUP_INDEX = 2;
    private static final int FILE_NAME_GROUP_INDEX = 3;
    private static final int LINE_NUMBER_GROUP_INDEX = 6;
    private static final int NON_LINE_NUMBER = -1;
    private static final int NATIVE_LINE_NUMBER = -2;

    private static boolean isAt(String line) {
        return line.startsWith("at ");
    }

    private static boolean isException(String line) {
        return EXCEPTION.matcher(line).find();
    }

    private static boolean isCommonFrames(String line) {
        return COMMON_FRAMES.matcher(line).find();
    }

    private static boolean isCausedBy(String line) {
        return line.startsWith(CAUSED_BY);
    }

    private static boolean isSuppressed(String line) {
        return line.startsWith(SUPPRESSED);
    }

    @SneakyThrows
    private static LogRecord parseLog(String logLine) {
        Matcher matcher = RECORD.matcher(logLine);
        if (matcher.find()) {
            String time = matcher.group(LOG_TIMESTAMP_GROUP_INDEX);
            String level = matcher.group(LOG_LEVEL_GROUP_INDEX);
            String thread = matcher.group(LOG_THREAD_GROUP_INDEX);
            String className = matcher.group(LOG_CLASS_GROUP_INDEX);
            String lineNumberStr = matcher.group(LOG_LINE_GROUP_INDEX);
            String message = matcher.group(LOG_MSG_GROUP_INDEX);
            if (time != null && lineNumberStr != null) {
                return LogRecord.of(message, FORMAT.get().parse(time).getTime(),
                        LOG_LEVEL.get(level).toInt(), thread, className,
                        Integer.parseInt(lineNumberStr), null);
            }
        }
        return null;
    }

    private static ErrorStack parseException(String exception) {
        Matcher matcher = EXCEPTION_LINE.matcher(exception);
        if (matcher.find()) {
            String exceptionClass = matcher.group(EXCEPTION_CLASS_GROUP_INDEX);
            String message = matcher.group(EXCEPTION_MESSAGE_GROUP_INDEX);
            return ErrorStack.of(exceptionClass, message);
        }
        System.out.println(exception);
        return null;
    }

    private static StackTraceElement parseAt(String at) {
        Matcher matcher = AT.matcher(at);
        if (matcher.find()) {
            String declaringClass = matcher.group(DECLARING_CLASS_GROUP_INDEX);
            String methodName = matcher.group(METHOD_NAME_GROUP_INDEX);
            String fileName = matcher.group(FILE_NAME_GROUP_INDEX);
            String lineNumberStr = matcher.group(LINE_NUMBER_GROUP_INDEX);
            int lineNumber = lineNumberStr==null ? NON_LINE_NUMBER : Integer.parseInt(lineNumberStr);
            if (NATIVE_METHOD.equals(methodName)) {
                lineNumber = NATIVE_LINE_NUMBER;
            }
            return new StackTraceElement(declaringClass, methodName, fileName, lineNumber);
        }
        return null;
    }
}
