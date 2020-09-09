package studio.greeks.vilomoyu.util.log;

import lombok.Data;

import java.util.Stack;

/**
 *
 * @author <a href="mailto:wuzhao-1@thunisoft.com>Zhao.Wu</a>
 * @description studio.greeks.vilomoyu.util.log vilomoyu
 * @date 2020/9/7 0007 11:39
 */
@Data
public class ExceptionEntity {
    private String clazz;
    private String message;
    private Stack<String> ats;
    private Stack<ExceptionEntity> caused;

    public ExceptionEntity() {
        ats = new Stack<>();
        caused = new Stack<>();
    }
}
