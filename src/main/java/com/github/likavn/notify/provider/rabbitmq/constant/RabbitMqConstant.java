package com.github.likavn.notify.provider.rabbitmq.constant;

/**
 * rabbitmq常量
 *
 * @author likavn
 * @since 2023/01/01
 */
public class RabbitMqConstant {
    private RabbitMqConstant() {
    }

    /**
     * rabbitMq交换机
     */
    public static final String EXCHANGE = "notify.exchange";

    /**
     * rabbitMq队列key；
     * 参数：
     * <p>
     * 1.Topic;
     */
    public static final String QUEUE = "notify.queue.%s.%s";

    /**
     * rabbitMq路由key；
     * 参数：
     * <p>
     * 1.Topic;
     */
    public static final String ROUTING = "notify.routing.key.%s";

    /**
     * 延时队列交换机
     * 参数：
     * <p>
     * 1.服务serviceId;
     */
    public static final String DELAY_EXCHANGE = "notify.delay.exchange.%s";

    /**
     * 延时队列名
     * 参数：
     * <p>
     * 1.服务serviceId;
     */
    public static final String DELAY_QUEUE = "notify.delay.queue.%s";

    /**
     * 延时队列路由key
     * 参数：
     * <p>
     * 1.服务serviceId;
     */
    public static final String DELAY_ROUTING_KEY = "notify.delay.routing.key.%s";
}
