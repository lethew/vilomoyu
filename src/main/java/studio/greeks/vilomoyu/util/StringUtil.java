package studio.greeks.vilomoyu.util;

import lombok.experimental.UtilityClass;

/**
 *
 * @author <a href="mailto:wuzhao-1@thunisoft.com>吴昭</a>
 */
@UtilityClass
public class StringUtil {
    public String substring(String src, int len) {
        if (src.length() <= len) {
            return src;
        } else {
            return src.substring(0, len);
        }
    }
}
