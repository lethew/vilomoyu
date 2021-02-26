package studio.greeks.vilomoyu.util;

import lombok.experimental.UtilityClass;

import java.util.UUID;

/**
 * 生成uuid的工具类
 *
 * @author hanfeng
 * @version 1.0
 */
@UtilityClass
public class UUIDUtil {
    /**
     * 得到uuid
     * <p>
     * 不包含连字符
     *
     * @return eg ：DE699704254E733E8E324CC0DD0B0C46
     */
    public String getUuid() {
        return uuid().toUpperCase();
    }

    /**
     * 减少UUID.toString的'-'符号，{@linkplain UUID#toString()}
     * @return string
     */
    private String uuid(){
        UUID uuid = UUID.randomUUID();
        return digits(uuid.getMostSignificantBits() >> 32, 8) +
                digits(uuid.getMostSignificantBits() >> 16, 4) +
                digits(uuid.getMostSignificantBits(), 4) +
                digits(uuid.getLeastSignificantBits() >> 48, 4) +
                digits(uuid.getLeastSignificantBits(), 12);
    }

    private static String digits(long val, int digits) {
        long hi = 1L << (digits * 4);
        return Long.toHexString(hi | (val & (hi - 1))).substring(1);
    }
}
