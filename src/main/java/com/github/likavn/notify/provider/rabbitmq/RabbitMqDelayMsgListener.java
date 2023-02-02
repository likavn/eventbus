package com.github.likavn.notify.provider.rabbitmq;

import com.github.likavn.notify.base.BaseDelayMsgHandler;
import com.github.likavn.notify.provider.rabbitmq.constant.RabbitMqConstant;
import com.github.likavn.notify.utils.SpringUtil;
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
 * 延时消息处理类
 *
 * @author likavn
 * @since 2023/01/01
 */
public class RabbitMqDelayMsgListener extends BaseDelayMsgHandler {
    private static final Logger logger = LoggerFactory.getLogger(RabbitMqDelayMsgListener.class);
    /**
     * 消费者个数
     */
    private static final byte CONCURRENCY = 3;

    /**
     * 是否初始化rabbitmq
     */
    private boolean isInitRabbitMq = false;

    @SuppressWarnings("all")
    public RabbitMqDelayMsgListener(CachingConnectionFactory connectionFactory) {
        Connection connection = connectionFactory.createConnection();
        byte count = 1;
        while (count++ <= CONCURRENCY) {
            init(connection);
        }
    }

    /**
     * 初始化
     *
     * @param connection connection
     */
    private void init(Connection connection) {
        try {
            Channel channel = connection.createChannel(false);

            // 初始化rabbitmq
            initRabbitMq(channel);

            DefaultConsumer defaultConsumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag,
                                           Envelope envelope,
                                           AMQP.BasicProperties properties,
                                           byte[] body) {
                    try {
                        receiver(body);
                    } catch (Exception ex) {
                        logger.error("DelayMessageListener", ex);
                    }
                }
            };
            String queueName = String.format(RabbitMqConstant.DELAY_QUEUE, SpringUtil.getServiceId());
            channel.basicConsume(queueName, true, defaultConsumer);
        } catch (Exception e) {
            logger.error("DelayMessageListener.initRabbitMq", e);
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
        String exchangeName = String.format(RabbitMqConstant.DELAY_EXCHANGE, SpringUtil.getServiceId());
        Map<String, Object> args = new HashMap<>(4);
        args.put("x-delayed-type", "direct");
        //属性参数 交换机名称 交换机类型 是否持久化 是否自动删除 配置参数
        channel.exchangeDeclare(exchangeName, "x-delayed-message", true, false, args);

        // 定义队列名称
        String queueName = String.format(RabbitMqConstant.DELAY_QUEUE, SpringUtil.getServiceId());

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
                String.format(RabbitMqConstant.DELAY_ROUTING_KEY, SpringUtil.getServiceId()));
        isInitRabbitMq = true;
    }
}
