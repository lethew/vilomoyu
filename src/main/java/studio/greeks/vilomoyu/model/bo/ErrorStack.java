package studio.greeks.vilomoyu.model.bo;

import lombok.Data;

import java.util.List;

/**
 *
 * @author <a href="mailto:wuzhao-1@thunisoft.com>吴昭</a>
 */
@Data
public class ErrorStack {
    private String eClass;
    private String message;
    private List<StackTraceElement> ats;
    private ErrorStack nested;
    /**
     * 关联类型：Caused by | Suppressed
     */
    private String nestedType;

    public static ErrorStack of(String className, String message) {
        ErrorStack stack = new ErrorStack();
        stack.setEClass(className);
        stack.setMessage(message);
        return stack;
    }
}
