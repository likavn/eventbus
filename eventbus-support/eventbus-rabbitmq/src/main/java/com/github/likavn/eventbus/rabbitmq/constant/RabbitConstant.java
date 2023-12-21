package com.github.likavn.eventbus.rabbitmq.constant;

/**
 * rabbitmq常量
 *
 * @author likavn
 * @since 2023/01/01
 */
public class RabbitConstant {
    private RabbitConstant() {
    }

    /**
     * rabbitMq交换机
     */
    public static final String EXCHANGE = "eventbus.exchange";

    /**
     * rabbitMq队列key；
     * 参数：
     * <p>
     * 1.Topic;
     */
    public static final String QUEUE = "eventbus.queue.%s.%s";

    /**
     * rabbitMq路由key；
     * 参数：
     * <p>
     * 1.Topic;
     */
    public static final String ROUTING = "eventbus.routing.key.%s";

    /**
     * 延时队列交换机
     * 参数：
     * <p>
     * 1.服务serviceId;
     */
    public static final String DELAY_EXCHANGE = "eventbus.delay.exchange.%s";

    /**
     * 延时队列名
     * 参数：
     * <p>
     * 1.服务serviceId;
     */
    public static final String DELAY_QUEUE = "eventbus.delay.queue.%s";

    /**
     * 延时队列路由key
     * 参数：
     * <p>
     * 1.服务serviceId;
     */
    public static final String DELAY_ROUTING_KEY = "eventbus.delay.routing.key.%s";
}
