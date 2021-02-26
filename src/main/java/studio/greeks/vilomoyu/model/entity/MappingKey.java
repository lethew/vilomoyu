package studio.greeks.vilomoyu.model.entity;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * TODO:
 *
 * @author <a href="mailto:wuzhao-1@thunisoft.com>吴昭</a>
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
@EqualsAndHashCode
public class MappingKey implements Serializable {
    @Column(nullable = false, length = 32)
    private String throwId;
    @Column(nullable = false, length = 32)
    private String stackId;
}
