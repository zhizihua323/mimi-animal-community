package org.mimi.redis.utils;

public class RedisConstant {
    public static final String USER_LOGIN_KEY_PREFIX = "user:login:";
    public static final int USER_LOGIN_KEY_TIMEOUT = 60 * 60 * 24 * 100;

    //动物档案
    public static final String ANIMAL_ARCHIVAL_KEY_PREFIX = "archival:data:";


    //扣减库存
    public static final String ORDER_STOCK_KEY_NX = "order:stock:";

    public static final String ORDER_STOCK_LUA_SCRIPT = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEY[1]) else return 0 end";
    //布隆过滤器
    public static final String GOODS_BLOOM_FILTER = "goods:bloom:";
    public static final String Archival_BLOOM_FILTER = "archival:bloom:";

}
