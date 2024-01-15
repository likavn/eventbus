package com.github.likavn.eventbus.provider.redis.constant;

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
     * 前缀
     */
    private static final String SUFFIX = "eventbus:";

    /**
     * 延时队列key
     * 参数：
     * <p>
     * 1.服务serviceId;
     */
    public static final String NOTIFY_DELAY_PREFIX = SUFFIX + "delay:%s";

    /**
     * 延时队列,延时消息处理超时时长key
     * 参数：
     * <p>
     * 1. value md5;
     */
    public static final String NOTIFY_DELAY_LOCK_PREFIX = SUFFIX + "delay.lock:%s";

    /**
     * 分布式锁,订阅消息锁
     * 参数：
     * <p>
     * 1. 服务serviceId;
     */
    public static final String PENDING_MSG_LOCK_PREFIX = "%s-%s";

    /**
     * 消息队列前缀
     * 参数：
     * <p>
     * 1.服务serviceId;
     */
    public static final String NOTIFY_SUBSCRIBE_PREFIX = SUFFIX + "subscribe:%s";
    /**
     * 消息队列前缀
     * 参数：
     * <p>
     * 1.服务serviceId;
     */
    public static final String NOTIFY_SUBSCRIBE_DELAY_PREFIX = SUFFIX + "subscribe-delay:%s";
}
