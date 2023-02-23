package com.github.likavn.notify.provider.redis.constant;

/**
 * redis常量
 *
 * @author likavn
 * @since 2023/01/01
 */
public class RedisConstant {
    private RedisConstant() {
    }

    /**
     * 延时队列key
     * 参数：
     * <p>
     * 1.服务serviceId;
     */
    public static final String REDIS_Z_SET_KEY = "notify.delay.key.v2:%s";

    /**
     * 消息订阅分布式锁
     * 参数：
     * <p>
     * 1.消息ID;
     */
    public static final String REDIS_LOCK_KEY = "notify.sub.lock:";
}
