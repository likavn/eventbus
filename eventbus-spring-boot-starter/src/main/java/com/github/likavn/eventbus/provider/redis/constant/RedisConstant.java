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
     * 分布式锁,订阅消息锁
     * 参数：
     * <p>
     * 1. 服务serviceId;
     */
    public static final String LOCK_PENDING_MSG_PREFIX = SUFFIX + "lock:{%s}:%s-%s";

    /**
     * 消息队列前缀
     * 参数：
     * <p>
     * 1.服务serviceId;
     */
    public static final String BUS_SUBSCRIBE_PREFIX = SUFFIX + "subscribe:{%s}";

    /**
     * 延时队列,延时消息处理超时时长key
     * 参数：
     * <p>
     * 1.服务serviceId;
     */
    public static final String BUS_DELAY_LOCK_PREFIX = SUFFIX + "lock:delay:{%s}";

    /**
     * 延时队列zset key
     * 参数：
     * <p>
     * 1.服务serviceId;
     */
    public static final String BUS_DELAY_PREFIX = SUFFIX + "delay:{%s}";
    /**
     * 延时消息通知Stream队列key前缀
     * 参数：
     * <p>
     * 1.服务serviceId;
     */
    public static final String BUS_DELAY_SUBSCRIBE_PREFIX = SUFFIX + "subscribe_delay:{%s}";
}
