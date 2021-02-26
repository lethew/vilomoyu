package studio.greeks.vilomoyu.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 异常与堆栈信息的映射表
 * @author <a href="mailto:wuzhao-1@thunisoft.com>吴昭</a>
 */
@Entity
@Table(name = "t_throwable_stack_trace_mapping")
@Data
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
public class ThrowableStackTraceMapping {

    @EmbeddedId
    private MappingKey traceMappingKey;

    private Integer index;
}
