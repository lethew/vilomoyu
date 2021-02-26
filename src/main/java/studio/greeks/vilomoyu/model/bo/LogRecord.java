package studio.greeks.vilomoyu.model.bo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 *
 * @author <a href="mailto:wuzhao-1@thunisoft.com>吴昭</a>
 */
@Data
@AllArgsConstructor(staticName = "of")
public class LogRecord {
    private String message;
    private Long timestamp;
    private Integer level;
    private String thread;
    private String className;
    private Integer lineNumber;
    private ErrorStack stack;
}
