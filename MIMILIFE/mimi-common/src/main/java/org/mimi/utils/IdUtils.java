package org.mimi.utils;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.IdUtil;

/**
 * ID生成工具类
 */
public class IdUtils {
    private static Snowflake snowflake = IdUtil.createSnowflake(1, NetUtil.ipv4ToLong(NetUtil.getLocalhostStr()) % 32);

    /**
     * 生成雪花ID
     */
    public static long getSnowflakeId() {
        return snowflake.nextId();
    }

    /**
     * 生成雪花ID字符串
     */
    public static String getSnowflakeIdStr() {
        return snowflake.nextIdStr();
    }

    /**
     * 生成UUID
     */
    public static String getUUID() {
        return IdUtil.simpleUUID();
    }
}
