package studio.greeks.vilomoyu.util.log;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.SneakyThrows;
import studio.greeks.vilomoyu.model.LogInfo;

/**
 * 日志实体
 *
 * @author <a href="mailto:wuzhao-1@thunisoft.com>Zhao.Wu</a>
 * @description studio.greeks.vilomoyu.util.log vilomoyu
 * @date 2020/9/7 0007 11:38
 */
@Data
public class LogEntity {
    private ObjectMapper objectMapper = new ObjectMapper();
    private String timestamp;
    private String level;
    private String threadId;
    private String classPath;
    private String message;
    private ExceptionEntity exception;


    @SneakyThrows
    public LogInfo toLogInfo() {
        return new LogInfo()
                .setClassPath(classPath)
                .setTimestamp(timestamp)
                .setLevel(level)
                .setMessage(message)
                .setThreadId(threadId)
                .setStack(objectMapper.writeValueAsString(exception));
    }
}
