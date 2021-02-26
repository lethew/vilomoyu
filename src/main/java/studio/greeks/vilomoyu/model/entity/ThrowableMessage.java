package studio.greeks.vilomoyu.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 *
 *
 * @author <a href="mailto:wuzhao-1@thunisoft.com>吴昭</a>
 */
@Entity
@Table(name = "t_throwable_message")
@Data
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
public class ThrowableMessage {
    @Id
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

    @Column(length = 500)
    private String message;

    private String logId;
}
