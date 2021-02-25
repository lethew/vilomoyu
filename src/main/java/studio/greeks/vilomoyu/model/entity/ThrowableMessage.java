package studio.greeks.vilomoyu.model.entity;

/**
 *
 *
 * @author <a href="mailto:wuzhao-1@thunisoft.com>吴昭</a>
 */
public class ThrowableMessage {
    private String id;
    private String pid;
    /**
     * 0:primary; 1:causedBy; 2:suppressed
     */
    private Integer type;

    /**
     * 异常类型class
     */
    private String cId;

    private String message;

    private String logId;
}
