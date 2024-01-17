package com.github.likavn.eventbus.provider.rabbit;

import com.github.likavn.eventbus.core.DeliveryBus;
import com.github.likavn.eventbus.core.base.Lifecycle;
import com.github.likavn.eventbus.core.constant.BusConstant;
import com.github.likavn.eventbus.core.metadata.BusConfig;
import com.github.likavn.eventbus.core.utils.Func;
import com.github.likavn.eventbus.provider.rabbit.constant.RabbitConstant;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.Connection;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 延时消息监听处理器
 *
 * @author likavn
 * @since 2023/01/01
 */
public class RabbitMsgDelayListener implements Lifecycle {
    private static final Logger logger = LoggerFactory.getLogger(RabbitMsgDelayListener.class);

    /**
     * 是否初始化rabbitmq
     */
    private boolean isInitRabbitMq = false;
    private final BusConfig config;
    private final DeliveryBus deliveryBus;
    private final CachingConnectionFactory connectionFactory;
    private Connection connection = null;

    @SuppressWarnings("all")
    public RabbitMsgDelayListener(BusConfig config,
                                  DeliveryBus deliveryBus,
                                  CachingConnectionFactory connectionFactory) {
        this.config = config;
        this.deliveryBus = deliveryBus;
        this.connectionFactory = connectionFactory;
    }

    @Override
    public void register() {
        this.connection = connectionFactory.createConnection();
        byte count = 1;
        while (count++ <= config.getConsumerCount()) {
            bindListener(connection);
        }
    }

    /**
     * 初始化
     *
     * @param connection connection
     */
    private void bindListener(Connection connection) {
        try {
            Channel channel = connection.createChannel(false);
            // 初始化rabbitmq
            initRabbitMq(channel);

            String queueName = String.format(RabbitConstant.QUEUE_DELAY, config.getServiceId());
            channel.basicConsume(queueName, false, new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag,
                                           Envelope envelope,
                                           AMQP.BasicProperties properties,
                                           byte[] body) throws IOException {
                    String oldName = Func.reThreadName(BusConstant.DELAY_MSG_THREAD_NAME);
                    try {
                        deliveryBus.deliverDelay(body);
                        channel.basicAck(envelope.getDeliveryTag(), false);
                    } catch (Exception ex) {
                        logger.error("RabbitMqDelayMsgListener", ex);
                        channel.basicNack(envelope.getDeliveryTag(), false, true);
                    } finally {
                        Thread.currentThread().setName(oldName);
                    }
                }
            });
        } catch (Exception e) {
            logger.error("RabbitMqDelayMsgListener.init", e);
        }
    }

    /**
     * 初始化rabbitmq
     *
     * @param channel channel
     * @throws IOException e
     */
    private void initRabbitMq(Channel channel) throws IOException {
        if (isInitRabbitMq) {
            return;
        }
        // 初始创建交换机
        String exchangeName = String.format(RabbitConstant.EXCHANGE_DELAY, config.getServiceId());
        Map<String, Object> args = new HashMap<>(4);
        args.put("x-delayed-type", "direct");
        //属性参数 交换机名称 交换机类型 是否持久化 是否自动删除 配置参数
        channel.exchangeDeclare(exchangeName, "x-delayed-message", true, false, args);

        // 定义队列名称
        String queueName = String.format(RabbitConstant.QUEUE_DELAY, config.getServiceId());

        // 声明一个队列。
        // 参数一：队列名称
        // 参数二：是否持久化
        // 参数三：是否排外  如果排外则这个队列只允许有一个消费者
        // 参数四：是否自动删除队列，如果为true表示没有消息也没有消费者连接自动删除队列
        // 参数五：队列的附加属性
        // 注意：
        // 1.声明队列时，如果已经存在则放弃声明，如果不存在则会声明一个新队列；
        // 2.队列名可以任意取值，但需要与消息接收者一致。
        // 3.下面的代码可有可无，一定在发送消息前确认队列名称已经存在RabbitMQ中，否则消息会发送失败。
        channel.queueDeclare(queueName, true, false, false, null);

        channel.queueBind(queueName, exchangeName,
                // 设置路由key
                String.format(RabbitConstant.ROUTING_KEY_DELAY, config.getServiceId()));
        isInitRabbitMq = true;
    }

    @Override
    public void destroy() {
        // destroy
        if (null != this.connection) {
            this.connection.close();
        }
    }
}
