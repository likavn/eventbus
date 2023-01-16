package com.github.likavn.notify.constant;

/**
 * 通知常量
 *
 * @author likavn
 * @since 2023/01/01
 */
public class MsgConstant {
    private MsgConstant() {
    }

    /**
     * rabbitMq交换机
     */
    public static final String EXCHANGE = "notify.exchange.v2";

    /**
     * rabbitMq队列key；
     * 参数：
     * <p>
     * 1.服务serviceId;
     * 2.业务code;
     * 3.处理器。
     */
    public static final String QUEUE = "notify.queue.v2.%s.%s.%s";

    /**
     * rabbitMq路由key；
     * 参数：
     * <p>
     * 1.服务serviceId;
     * 2.业务code。
     */
    public static final String ROUTING = "notify.routing.key.v2.%s.%s";

    /**
     * 延时队列交换机
     * 参数：
     * <p>
     * 1.服务serviceId;
     */
    public static final String DELAY_EXCHANGE = "notify.delay.exchange.v2.%s";

    /**
     * 延时队列名
     * 参数：
     * <p>
     * 1.服务serviceId;
     */
    public static final String DELAY_QUEUE = "notify.delay.queue.v2.%s";

    /**
     * 延时队列路由key
     * 参数：
     * <p>
     * 1.服务serviceId;
     */
    public static final String DELAY_ROUTING_KEY = "notify.delay.routing.key.v2.%s";

    /**
     * 延时队列key
     * 参数：
     * <p>
     * 1.服务serviceId;
     */
    public static final String REDIS_Z_SET_KEY = "notify.delay.key.v2:%s";
}
