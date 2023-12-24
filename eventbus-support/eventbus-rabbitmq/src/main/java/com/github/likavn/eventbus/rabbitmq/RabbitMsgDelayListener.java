package com.github.likavn.eventbus.rabbitmq;


import com.github.likavn.eventbus.core.DeliverBus;
import com.github.likavn.eventbus.core.base.Lifecycle;
import com.github.likavn.eventbus.core.constant.MsgConstant;
import com.github.likavn.eventbus.core.metadata.BusConfig;
import com.github.likavn.eventbus.core.utils.Func;
import com.github.likavn.eventbus.rabbitmq.constant.RabbitConstant;
import com.rabbitmq.client.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * 延时消息监听处理器
 *
 * @author likavn
 * @since 2023/01/01
 */
@Slf4j
public class RabbitMsgDelayListener implements Lifecycle {
    /**
     * 是否初始化rabbitmq
     */
    private boolean isInitRabbitMq = false;
    private final ConnectionFactory connectionFactory;
    private final BusConfig config;
    private final DeliverBus deliverBus;
    private List<Connection> connections = null;
    private List<Channel> channels = null;

    @SuppressWarnings("all")
    public RabbitMsgDelayListener(ConnectionFactory connectionFactory, BusConfig config, DeliverBus deliverBus) {
        this.connectionFactory = connectionFactory;
        this.config = config;
        this.deliverBus = deliverBus;
    }

    @Override
    public void register() throws IOException, TimeoutException {
        connections = new ArrayList<>(1);
        channels = new ArrayList<>(2);
        Connection connection = connectionFactory.newConnection();
        connections.add(connection);
        byte count = 1;
        while (count++ <= config.getConsumerNum()) {
            Channel channel = connection.createChannel();
            channels.add(channel);
            bindListener(channel);
        }
    }

    /**
     * 初始化
     *
     * @param channel channel
     */
    private void bindListener(Channel channel) {
        try {
            // 初始化rabbitmq
            initRabbitMq(channel);

            String queueName = String.format(RabbitConstant.QUEUE_DELAY, config.getServiceId());
            channel.basicConsume(queueName, false, new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag,
                                           Envelope envelope,
                                           AMQP.BasicProperties properties,
                                           byte[] body) throws IOException {
                    String oldName = Func.reThreadName(MsgConstant.DELAY_MSG_THREAD_NAME);
                    try {
                        deliverBus.deliverDelay(body);
                        channel.basicAck(envelope.getDeliveryTag(), false);
                    } catch (Exception ex) {
                        log.error("RabbitMqDelayMsgListener", ex);
                        channel.basicNack(envelope.getDeliveryTag(), false, true);
                    } finally {
                        Thread.currentThread().setName(oldName);
                    }
                }
            });
        } catch (Exception e) {
            log.error("RabbitMqDelayMsgListener.init", e);
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
