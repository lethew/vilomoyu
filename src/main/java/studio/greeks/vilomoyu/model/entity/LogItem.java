package studio.greeks.vilomoyu.model.entity;


/**
 *
 * @author <a href="mailto:wuzhao-1@thunisoft.com>吴昭</a>
 */
public class LogItem {
    private String id;

    private String systemId;
    /**
     * 系统所在地区
     */
    private String area;
    /**
     * 日志标记，用于根据标记批量删除
     */
    private String importTag;

    private Long timestamp;

    private Integer level;

    private String threadId;

    private String className;

    private Integer lineNumber;

    private String message;
}
