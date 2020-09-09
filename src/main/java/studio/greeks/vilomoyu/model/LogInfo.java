package studio.greeks.vilomoyu.model;

import lombok.Data;
import lombok.experimental.Accessors;
import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;
import org.influxdb.annotation.TimeColumn;

/**
 * 日志信息
 *
 * @author <a href="mailto:wuzhao-1@thunisoft.com>Zhao.Wu</a>
 * @description studio.greeks.vilomoyu.model vilomoyu
 * @date 2020/9/1 0001 14:25
 */
@Data
@Accessors(chain = true)
@Measurement(name = "log_info")
public class LogInfo {
    /**
     * 日志记录时间
     */
    @TimeColumn
    private String timestamp;
    /**
     * 所属系统编号
     */
    @Column(name = "sysId", tag = true)
    private String sysId;
    /**
     * 系统所在地区
     */
    @Column(name = "area", tag = true)
    private String area;
    /**
     * 日志标记，用于根据标记批量删除
     */
    @Column(name = "importTag", tag = true)
    private String importTag;
    /**
     * 日志级别
     */
    @Column(name = "level", tag = true)
    private String level;
    /**
     * 日志记录线程编号
     */
    @Column(name = "threadId", tag = true)
    private String threadId;

    /**
     * 日志记录类和代码行数
     */
    @Column(name = "classPath", tag = true)
    private String classPath;

    /**
     * 日志具体信息
     */
    @Column(name = "message")
    private String message;

    /**
     * 日志异常堆栈信息
     */
    @Column(name = "stack")
    private String stack;
}
