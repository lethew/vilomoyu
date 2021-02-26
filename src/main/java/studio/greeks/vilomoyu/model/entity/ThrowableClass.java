package studio.greeks.vilomoyu.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Objects;

/**
 *
 * @author <a href="mailto:wuzhao-1@thunisoft.com>吴昭</a>
 */
@Entity
@Table(name = "t_throwable_class")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ThrowableClass {
    @Id
    private String id;
    private String className;

    @Override
    public int hashCode(){
        return id !=null ? id.hashCode() : super.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ThrowableClass that = (ThrowableClass) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(className, that.className);
    }
}
