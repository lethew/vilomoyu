package studio.greeks.vilomoyu.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import studio.greeks.vilomoyu.model.LogInfo;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Stack;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * @author <a href="mailto:wuzhao-1@thunisoft.com>Zhao.Wu</a>
 * @description studio.greeks.vilomoyu.util vilomoyu
 * @date 2020/9/2 0002 16:27
 */
@Slf4j
public class LogAnalyzer {
    private static final Pattern TIME_REGX = Pattern.compile("[0-9]{4}-[0-9]{2}-[0-9]{2}\\s[0-9]{2}:[0-9]{2}:[0-9]{2}[:,][0-9]{3}");
    private static final Pattern EXCEPTION_REGX = Pattern.compile("[\\S]+((Exception)|(Error)|(Throwable)):[\\S\\s]+");
    private static final Pattern TAG_REGX = Pattern.compile("\\[[A-Za-z0-9:\\-.\\s]+]");
    private static final String CAUSED = "Caused";
    private static final String AT = "at";
    private final BlockingQueue<Log> queue = new LinkedBlockingQueue<>();
    private final AtomicBoolean active = new AtomicBoolean(Boolean.TRUE);
    private final AtomicBoolean hasBatch = new AtomicBoolean(Boolean.TRUE);

    public void parse(Stream<String> stream) {
        ThreadPoolUtil.execute(() -> parseLog(stream));
    }

    public BlockingQueue<BatchPoints> logInfos(Stream<String> stream, LogInfo example) {
        parse(stream);
        BlockingQueue<BatchPoints> points = new LinkedBlockingQueue<>();
        ThreadPoolUtil.execute(()->buildBatchPoints(example, points));
        return points;
    }

    private void parseLog(Stream<String> stream) {
        AtomicReference<Log> cur = new AtomicReference<>();
        AtomicBoolean inStack = new AtomicBoolean(false);
        AtomicBoolean inCaused = new AtomicBoolean(false);
        stream.map(String::trim)
                .filter(str -> !str.isEmpty())
                .forEach(line -> {
                    Matcher timeMatcher = TIME_REGX.matcher(line);
                    Matcher exceptionMatcher = EXCEPTION_REGX.matcher(line);
                    boolean caused = line.trim().startsWith(CAUSED);
                    // 解析第一行，这个跟日志记录的表达式有关系，优化可以根据日志表达式解析
                    if (timeMatcher.find()) {
                        Log previous = cur.get();
                        if (previous != null) {
                            try {
                                queue.put(previous);
                            } catch (InterruptedException e) {
                                log.error(e.getLocalizedMessage(), e);
                            }
                        }
                        cur.set(new Log());
                        cur.get().timestamp = timeMatcher.group();
                        Matcher matcher = TAG_REGX.matcher(line);
                        if (matcher.find()) {
                            cur.get().level = line.substring(matcher.start()+1, matcher.end()-1);
                        }
                        if (matcher.find()) {
                            cur.get().threadId = line.substring(matcher.start()+1, matcher.end()-1);
                        }
                        int end = 0;
                        if (matcher.find()) {
                            end = matcher.end();
                            cur.get().classPath = line.substring(matcher.start() + 1, end - 1);
                        }
                        String[] split = line.substring(end).split("-", 2);
                        cur.get().setMessage(split[1]);
                        inStack.set(false);
                        inCaused.set(false);
                    }
                    // 解析Java异常日志栈信息
                    else if (cur.get() != null) {
                        // 解析异常数据首行
                        if (!caused && exceptionMatcher.find()) {
                            cur.get().exception = new Exception();
                            String[] split = line.split(":", 2);
                            cur.get().exception.clazz = split[0];
                            cur.get().exception.message = split[1];
                            inStack.set(true);
                        }
                        // 解析caused by 数据，异常存在多个caused by， 所以需要进行压栈
                        else if (caused && cur.get().exception != null) {
                            cur.get().exception.caused.push(new Exception());
                            Exception peek = cur.get().exception.caused.peek();
                            String[] split = line.split(":", 3);
                            peek.clazz = split[1];
                            peek.message = split[2];
                            inCaused.set(true);
                        }
                        // 解析at行
                        else if (line.trim().startsWith(AT)) {
                            try {
                                if (cur.get().exception.caused == null) {
                                    cur.get().exception.ats.push(line);
                                } else {
                                    Exception peek = cur.get().exception.caused.peek();
                                    peek.ats.push(line);
                                }
                            }catch (java.lang.Exception e) {
                                log.error(line);
                            }
                        }
                        // 解析异常消息换行
                        else if (inStack.get() && !inCaused.get()) {
                            cur.get().exception.message += "\n\r"+line;
                        }
                        // 解析caused by 消息换行
                        else if (inStack.get() && inCaused.get()) {
                            Exception peek = cur.get().exception.caused.peek();
                            peek.message += "\n\r"+line;
                        }
                    }
                    // 解析日志消息换行信息
                    else if (!inCaused.get() && !inStack.get()) {
                        cur.get().message += "\n\r"+line;
                    }
                });
        if (cur.get() != null) {
            try {
                queue.put(cur.get());
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }
        }
        active.compareAndSet(true, false);
    }

    private void buildBatchPoints(LogInfo example, BlockingQueue<BatchPoints> points) {
        ObjectMapper objectMapper = new ObjectMapper();
        int i = 0;
        BatchPoints batch = BatchPoints.builder()
                .tag("async", "true")
                .consistency(InfluxDB.ConsistencyLevel.ALL)
                .build();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        while (active.get()) {
            try {
                Log take = queue.take();
                LogInfo logInfo = new LogInfo()
                        .setTimestamp(take.timestamp)
                        .setLevel(take.level)
                        .setMessage(take.message)
                        .setThreadId(take.threadId)
                        .setClassPath(take.classPath)
                        .setStack(objectMapper.writeValueAsString(take.exception))
                        .setArea(example.getArea())
                        .setSysId(example.getSysId())
                        .setImportTag(example.getImportTag());
                Point point = Point.measurementByPOJO(LogInfo.class)
                        .time(dateFormat.parse(take.getTimestamp()).getTime(), TimeUnit.MILLISECONDS)
                        .addFieldsFromPOJO(logInfo)
                        .build();
                batch.point(point);
                if(++i > 100) {
                    i = 0;
                    points.put(batch);
                    batch = BatchPoints.builder()
                            .tag("async", "true")
                            .consistency(InfluxDB.ConsistencyLevel.ALL)
                            .build();
                }
            } catch (InterruptedException | JsonProcessingException | ParseException e) {
                log.error(e.getMessage(), e);
            }
        }
        if (batch.getPoints().size() > 0) {
            points.add(batch);
        }
        hasBatch.compareAndSet(true, false);
    }

    public BlockingQueue<Log> getQueue() {
        return queue;
    }

    public boolean isActive() {
        return active.get();
    }

    public boolean hasBatch() {
        return hasBatch.get();
    }

    @Data
    private static class Log {
        private String timestamp;
        private String level;
        private String threadId;
        private String classPath;
        private String message;
        private Exception exception;
    }

    @Data
    private static class Exception {
        private String clazz;
        private String message;
        private Stack<String> ats;
        private Stack<Exception> caused;

        public Exception() {
            ats = new Stack<>();
            caused = new Stack<>();
        }
    }

    public static void main(String[] args) throws IOException {
//        String path = "D:\\Document\\智能保全系统\\现场日志\\20200513\\广西\\znbq\\znbq_147.72.200.67_8080.2020-04-23.0.log";
        File dir = new File("D:\\Document\\智能保全系统\\现场日志\\20200513\\");


        FileUtil.readDir(dir, file -> file.getName().endsWith(".log"), stream -> {
            InfluxDB influx = InfluxDBFactory.connect("http://172.25.16.81:8086","admin", "123qwe");
            influx.setDatabase("vilomoyu")
                    .enableBatch(100, 2000, TimeUnit.MILLISECONDS);
            influx.setLogLevel(InfluxDB.LogLevel.BASIC);
            LogAnalyzer logAnalyzer = new LogAnalyzer();
            BlockingQueue<BatchPoints> points = logAnalyzer.logInfos(stream, new LogInfo().setArea("gxgy").setSysId("znbq").setImportTag("test002"));
            while (logAnalyzer.hasBatch()) {
                BatchPoints take = null;
                try {
                    take = points.take();
                    log.info(take.toString());
                } catch (InterruptedException e) {
                    log.error(e.getMessage(), e);
                }
                influx.write(take);
            }

        });



    }
}
