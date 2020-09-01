package studio.greeks.vilomoyu.model;

import lombok.Data;
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
@Measurement(name = "log_info")
public class LogInfo {
    @TimeColumn
    private String timestamp;
    @Column(name = "sysId", tag = true)
    private String sysId;
    @Column(name = "area", tag = true)
    private String area;
    @Column(name = "tag", tag = true)
    private String tag;
    @Column(name = "importTag", tag = true)
    private String importTag;
    @Column(name = "level", tag = true)
    private String level;
    @Column(name = "threadId", tag = true)
    private String threadId;
    @Column(name = "message")
    private String message;
}
