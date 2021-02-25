package studio.greeks.vilomoyu.model.log;

import lombok.Data;

/**
 *
 * @author <a href="mailto:wuzhao-1@thunisoft.com>吴昭</a>
 */
@Data
public class LogRecord {
    private String message;
    private ErrorStack stack;
}
