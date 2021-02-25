package studio.greeks.vilomoyu.util;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import lombok.experimental.UtilityClass;

/**
 *
 * @author <a href="mailto:wuzhao-1@thunisoft.com>吴昭</a>
 */
@UtilityClass
public class HashUtil {
    private static final byte[] MD5_KEY = "vilomoyu2021".getBytes();
    public String md5(String str) {
        return Hashing.hmacMd5(MD5_KEY).newHasher().putString(str, Charsets.UTF_8).hash().toString();
    }
}
