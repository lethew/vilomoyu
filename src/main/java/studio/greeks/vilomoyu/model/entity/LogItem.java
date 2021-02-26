package studio.greeks.vilomoyu.model.entity;


import com.google.common.base.Strings;
import lombok.Data;
import org.h2.util.StringUtils;
import studio.greeks.vilomoyu.model.bo.LogRecord;
import studio.greeks.vilomoyu.util.StringUtil;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 *
 * @author <a href="mailto:wuzhao-1@thunisoft.com>吴昭</a>
 */
@Entity
@Table(name = "t_log_item")
@Data
public class LogItem {

    @Id
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

    @Column(length = 500)
    private String message;

    public static LogItem of(LogRecord record) {
        LogItem logItem = new LogItem();
        logItem.setTimestamp(record.getTimestamp());
        logItem.setLevel(record.getLevel());
        logItem.setThreadId(record.getThread());
        logItem.setClassName(record.getClassName());
        logItem.setLineNumber(record.getLineNumber());
        logItem.setMessage(StringUtil.substring(record.getMessage(), 500));
        return logItem;
    }
}
