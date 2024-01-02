package com.github.likavn.eventbus.rabbitmq;

import com.github.likavn.eventbus.core.DeliveryBus;
import com.github.likavn.eventbus.core.base.Lifecycle;
import com.github.likavn.eventbus.core.constant.BusConstant;
import com.github.likavn.eventbus.core.metadata.BusConfig;
import com.github.likavn.eventbus.core.metadata.support.Subscriber;
import com.github.likavn.eventbus.core.utils.Func;
import com.github.likavn.eventbus.rabbitmq.constant.RabbitConstant;
import com.rabbitmq.client.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * rabbitMq及时消息订阅器
 *
 * @author likavn
 * @date 2024/01/01
 **/
@Slf4j
public class RabbitMsgSubscribeListener implements Lifecycle {
    /**
     * 是否创建交换机
     */
    private boolean isCreateExchange = false;
    private final ConnectionFactory connectionFactory;
    private final List<Subscriber> subscribers;
    private final DeliveryBus deliveryBus;
    private final BusConfig config;
    private List<Connection> connections = null;
    private List<Channel> channels = null;

    @SuppressWarnings("all")
    public RabbitMsgSubscribeListener(ConnectionFactory connectionFactory,
                                      DeliveryBus deliveryBus, BusConfig config, List<Subscriber> subscribers) {
        this.connectionFactory = connectionFactory;
        this.subscribers = subscribers;
        this.deliveryBus = deliveryBus;
        this.config = config;
    }

    @Override
    public void register() throws IOException, TimeoutException {
        if (Func.isEmpty(subscribers)) {
            return;
        }
        connections = new ArrayList<>(1);
        channels = new ArrayList<>(2);
        Connection connection = connectionFactory.newConnection();
        connections.add(connection);
        Integer consumerNum = config.getConsumerNum();
        for (Subscriber subscriber : subscribers) {
            int num = 0;
            while (num++ < consumerNum) {
                Channel channel = connection.createChannel();
                channels.add(channel);
                bindListener(subscriber, channel);
            }
        }
    }

    /**
     * mq监听绑定
     *
     * @param newConnection newConnection
     * @param code          消息类型
     */
    @SuppressWarnings("all")
    private void bindListener(Subscriber subscriber, Channel channel) {
        try {
            // 初始创建交换机
            createExchange(channel);

            // 定义队列名称
            String queueName = String.format(RabbitConstant.QUEUE,
                    subscriber.getTopic(), subscriber.getTrigger().getInvokeBean().getClass().getName());

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
            channel.queueBind(queueName, RabbitConstant.EXCHANGE, String.format(RabbitConstant.ROUTING, subscriber.getTopic()));
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
                        log.error("RabbitMsgSubscribeListener.bindListener", ex);
                        channel.basicNack(envelope.getDeliveryTag(), false, true);
                    } finally {
                        Thread.currentThread().setName(oldName);
                    }
                }
            });
        } catch (Exception e) {
            log.error("RabbitMsgSubscribeListener.bindListener", e);
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
        // 初始创建交换机
        String exchangeName = String.format(RabbitConstant.EXCHANGE, config.getServiceId());
        channel.exchangeDeclare(exchangeName,
                BuiltinExchangeType.TOPIC, true, false, Collections.emptyMap());
        isCreateExchange = true;
    }

    @Override
    public void destroy() throws IOException, TimeoutException {
        if (!Func.isEmpty(channels)) {
            for (Channel channel : channels) {
                channel.close();
            }
        }
        if (!Func.isEmpty(connections)) {
            for (Connection connection : connections) {
                connection.close();
            }
        }
    }
}
