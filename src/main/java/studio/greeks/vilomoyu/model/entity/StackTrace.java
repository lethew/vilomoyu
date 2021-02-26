package studio.greeks.vilomoyu.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import studio.greeks.vilomoyu.util.HashUtil;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Objects;

/**
 * 异常信息中的堆栈信息
 * @author <a href="mailto:wuzhao-1@thunisoft.com>吴昭</a>
 */
@Entity
@Table(name = "t_stack_trace")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StackTrace {
    @Id
    private String id;
    private String className;
    private String methodName;
    private String fileName;
    private Integer lineNumber;

    public static StackTrace of(StackTraceElement element) {
        String id = HashUtil.md5(element.toString());
        return new StackTrace(id, element.getClassName(), element.getMethodName(), element.getFileName(), element.getLineNumber());
    }

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
        StackTrace that = (StackTrace) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(className, that.className) &&
                Objects.equals(methodName, that.methodName) &&
                Objects.equals(fileName, that.fileName) &&
                Objects.equals(lineNumber, that.lineNumber);
    }
}
