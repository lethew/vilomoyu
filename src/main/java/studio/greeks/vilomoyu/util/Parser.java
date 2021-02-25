package studio.greeks.vilomoyu.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.weaver.SignatureUtils;
import studio.greeks.vilomoyu.model.log.ErrorStack;
import studio.greeks.vilomoyu.model.log.LogRecord;
import sun.security.provider.MD5;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TODO:
 *
 * @author <a href="mailto:wuzhao-1@thunisoft.com>吴昭</a>
 */
@Slf4j
public class Parser {
    private static final Pattern RECORD = Pattern.compile("^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}");
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

    private final List<LogRecord> records;

    private final List<StackTraceElement> elements = new LinkedList<>();

    public Parser(String path) throws FileNotFoundException {
        this(new FileInputStream(path));
    }

    public Parser(InputStream inputStream) {
        records = new LinkedList<>();
        try (InputStreamReader reader = new InputStreamReader(inputStream);
             BufferedReader br = new BufferedReader(reader)) {
            foreachLine(br);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
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
                    if (isNewRecord(line)) {
                        newRecord(currentRecord, lastIsLog, line, record);
                    } else if (record != null && isException(line)) {
                        newException(currentStack, lastIsLog, line, record);
                    } else if (record != null && isAt(line)) {
                        newStackTrace(lastIsLog, line, stack);
                    } else if (record != null && isCausedBy(line)) {
                        newSubException(currentStack, lastIsLog, line, stack, "Caused");
                    } else if (record != null && isSuppressed(line)) {
                        newSubException(currentStack, lastIsLog, line, stack, "Suppressed");
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
        elements.add(at);
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

    private void newRecord(AtomicReference<LogRecord> currentRecord, AtomicBoolean lastIsLog, String line, LogRecord record) {
        if (record != null) {
            records.add(record);
        }
        currentRecord.set(new LogRecord());
        record = currentRecord.get();
        record.setMessage(line);
        lastIsLog.set(true);
    }

    public List<LogRecord> getRecords() {
        return records;
    }

    public List<StackTraceElement> getElements() {
        return elements;
    }

    private static boolean isAt(String line) {
        return line.startsWith("at ");
    }

    private static boolean isException(String line) {
        return EXCEPTION.matcher(line).find();
    }

    private static boolean isCommonFrames(String line) {
        return COMMON_FRAMES.matcher(line).find();
    }

    private static boolean isNewRecord(String line) {
        return RECORD.matcher(line).find();
    }

    private static boolean isCausedBy(String line) {
        return line.startsWith("Caused by:");
    }

    private static boolean isSuppressed(String line) {
        return line.startsWith("Suppressed:");
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
            return new StackTraceElement(declaringClass, methodName, fileName, lineNumberStr==null ? NON_LINE_NUMBER : Integer.parseInt(lineNumberStr));
        }
        return null;
    }

    public static void main(String[] args) throws FileNotFoundException, JsonProcessingException {
        Parser parser = new Parser("D:\\Users\\Zhao\\Documents\\唐正强\\RIZHI\\新建文件夹\\znbq_stderr_127.0.0.1_8080.2021-01-26.0.log");
        Parser parser1 = new Parser("D:\\Users\\Zhao\\Documents\\唐正强\\RIZHI\\新建文件夹\\znbq_stderr_127.0.0.1_8080.2021-01-27.0.log");
        Parser parser2 = new Parser("D:\\Users\\Zhao\\Documents\\唐正强\\RIZHI\\新建文件夹\\znbq_stderr_127.0.0.1_8080.2021-01-28.0.log");
        long len = 0L;
        Set<String> set = new TreeSet<>();
        Map<String, String> map = new TreeMap<>();
        ObjectMapper mapper = new ObjectMapper();
        for (Parser parser3 : new Parser[]{parser, parser2, parser1}) {
            for (LogRecord record : parser3.getRecords()) {
                System.out.println(mapper.writeValueAsString(record));
                System.out.println();
                System.out.println();
            }
//            for (StackTraceElement element : parser3.getElements()) {
//                if (element != null) {
//                    String s = element.toString();
//                    set.add(s);
//                    len += s.length();
//                    String l = HashUtil.md5(s);
//                    map.put(l, s);
//                }
//            }
        }

//        System.out.println(set.size());
//        System.out.println(map.size());
//
//        long len1 = 0L;
//        for (Map.Entry<String, String> longStringEntry : map.entrySet()) {
//            if (longStringEntry.getValue().contains("znbq")) {
//                System.out.println(longStringEntry.getKey() + ":" + longStringEntry.getValue());
//            }
//            len1 += longStringEntry.getValue().length();
//        }
//        System.out.println(len);
//        System.out.println(len1);
//        System.out.println(64*set.size()+len1);

    }
}
