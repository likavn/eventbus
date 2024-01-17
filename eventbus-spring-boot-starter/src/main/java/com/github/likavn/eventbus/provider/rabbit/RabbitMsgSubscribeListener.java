package com.github.likavn.eventbus.provider.rabbit;

import com.github.likavn.eventbus.core.DeliveryBus;
import com.github.likavn.eventbus.core.base.Lifecycle;
import com.github.likavn.eventbus.core.constant.BusConstant;
import com.github.likavn.eventbus.core.metadata.BusConfig;
import com.github.likavn.eventbus.core.metadata.support.Subscriber;
import com.github.likavn.eventbus.core.utils.Func;
import com.github.likavn.eventbus.provider.rabbit.constant.RabbitConstant;
import com.rabbitmq.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.Connection;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * rabbitMq消息订阅器
 *
 * @author likavn
 * @since 2023/01/01
 **/
public class RabbitMsgSubscribeListener implements Lifecycle {
    private static final Logger logger = LoggerFactory.getLogger(RabbitMsgSubscribeListener.class);
    /**
     * 是否创建交换机
     */
    private boolean isCreateExchange = false;
    private final BusConfig config;
    private final DeliveryBus deliveryBus;
    private final List<Subscriber> subscribers;
    private final CachingConnectionFactory connectionFactory;
    private final String serviceExchangeName;
    private Connection connection = null;

    public RabbitMsgSubscribeListener(BusConfig config,
                                      DeliveryBus deliveryBus,
                                      List<Subscriber> subscribers,
                                      CachingConnectionFactory connectionFactory) {
        this.config = config;
        this.deliveryBus = deliveryBus;
        this.subscribers = subscribers;
        this.connectionFactory = connectionFactory;
        this.serviceExchangeName = String.format(RabbitConstant.EXCHANGE, config.getServiceId());
    }

    @Override
    public void register() {
        this.connection = connectionFactory.createConnection();
        for (Subscriber subscriber : subscribers) {
            int num = 0;
            while (num++ < config.getConsumerCount()) {
                createSubscriber(subscriber, connection);
            }
        }
    }

    /**
     * 绑定监听器
     *
     * @param subscriber    订阅者
     * @param newConnection 连接
     */
    @SuppressWarnings("all")
    private void createSubscriber(Subscriber subscriber, Connection newConnection) {
        try {
            Channel channel = newConnection.createChannel(false);
            // 初始创建交换机
            createExchange(channel);

            // 定义队列名称
            String queueName = String.format(RabbitConstant.QUEUE, subscriber.getTopic(), subscriber.getTrigger().getDeliverId());

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
            // 设置路由key
            channel.queueBind(queueName, serviceExchangeName, String.format(RabbitConstant.ROUTING, subscriber.getTopic()));
            // 接收消息。会持续坚挺，不能关闭channel和Connection
            // 参数一：队列名称
            // 参数二：消息是否自动确认，true表示自动确认接收完消息以后会自动将消息从队列移除。否则需要手动ack消息
            // 参数三：消息接收者
            channel.basicConsume(queueName, false, new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag,
                                           Envelope envelope,
                                           AMQP.BasicProperties properties,
                                           byte[] body) throws IOException {
                    String oldName = Func.reThreadName(BusConstant.SUBSCRIBE_MSG_THREAD_NAME);
                    try {
                        deliveryBus.deliver(subscriber, body);
                        channel.basicAck(envelope.getDeliveryTag(), false);
                    } catch (Exception ex) {
                        logger.error("createSubscriber", ex);
                        channel.basicNack(envelope.getDeliveryTag(), false, true);
                    } finally {
                        Thread.currentThread().setName(oldName);
                    }
                }
            });
        } catch (Exception e) {
            logger.error("createSubscriber", e);
        }
    }

    /**
     * 创建交换机
     *
     * @param channel 通道
     * @throws IOException e
     */
    private void createExchange(Channel channel) throws IOException {
        if (isCreateExchange) {
            return;
        }
        channel.exchangeDeclare(serviceExchangeName,
                BuiltinExchangeType.TOPIC, true, false, Collections.emptyMap());
        isCreateExchange = true;
    }

    @Override
    public void destroy() {
        // destroy
        if (null != this.connection) {
            this.connection.close();
        }
    }
}
