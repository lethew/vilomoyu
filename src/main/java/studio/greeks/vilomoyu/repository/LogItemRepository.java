package studio.greeks.vilomoyu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import studio.greeks.vilomoyu.model.entity.LogItem;

/**
 *
 * @author <a href="mailto:wuzhao-1@thunisoft.com>吴昭</a>
 */
public interface LogItemRepository extends JpaRepository<LogItem, String> {
}
