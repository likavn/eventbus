package com.github.likavn.eventbus.rabbitmq.constant;

/**
 * rabbitmq常量
 *
 * @author likavn
 * @date 2024/01/01
 */
public class RabbitConstant {
    private RabbitConstant() {
    }

    /**
     * 前缀
     */
    private static final String SUFFIX = "eventbus.";

    /**
     * rabbitMq及时消息交换机
     * 参数：
     * <p>
     * 1.服务serviceId;
     */
    public static final String EXCHANGE = SUFFIX + "exchange.%s";

    /**
     * rabbitMq队列key；
     * 参数：
     * <p>
     * 1.Topic;
     * 2.处理器全类名;
     */
    public static final String QUEUE = SUFFIX + "queue.%s.%s";

    /**
     * rabbitMq路由key；
     * 参数：
     * <p>
     * 1.Topic;
     */
    public static final String ROUTING = SUFFIX + "routing.key.%s";

    /**
     * 延时队列交换机
     * 参数：
     * <p>
     * 1.服务serviceId;
     */
    public static final String EXCHANGE_DELAY = SUFFIX + "exchange.delay.%s";

    /**
     * 延时队列名
     * 参数：
     * <p>
     * 1.服务serviceId;
     */
    public static final String QUEUE_DELAY = SUFFIX + "queue.delay.%s";

    /**
     * 延时队列路由key
     * 参数：
     * <p>
     * 1.服务serviceId;
     */
    public static final String ROUTING_KEY_DELAY = SUFFIX + "routing.key.delay.%s";
}
