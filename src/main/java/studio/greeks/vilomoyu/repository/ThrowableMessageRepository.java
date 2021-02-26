package studio.greeks.vilomoyu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import studio.greeks.vilomoyu.model.entity.LogItem;
import studio.greeks.vilomoyu.model.entity.ThrowableMessage;

/**
 *
 * @author <a href="mailto:wuzhao-1@thunisoft.com>吴昭</a>
 */
public interface ThrowableMessageRepository extends JpaRepository<ThrowableMessage, String> {
}
