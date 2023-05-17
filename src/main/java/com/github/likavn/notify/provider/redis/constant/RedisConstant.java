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
    public static final String NOTIFY_DELAY_PREFIX = "notify:delay:%s";

    /**
     * 延时队列,延时消息处理超时时长key
     * 参数：
     * <p>
     * 1. value md5;
     */
    public static final String NOTIFY_DELAY_LOCK_PREFIX = "notify:delay.lock:%s";

    /**
     * 消息队列前缀
     * 参数：
     * <p>
     * 1.服务serviceId;
     */
    public static final String NOTIFY_SUBSCRIBE_PREFIX = "notify:subscribe:%s";
}
