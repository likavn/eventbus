package com.github.likavn.notify.provider.rabbitmq;

import com.github.likavn.notify.base.BaseDelayMsgHandler;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.Connection;

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

    @SuppressWarnings("all")
    public RabbitMqDelayMsgListener(CachingConnectionFactory connectionFactory, Queue delayQueue) {
        Connection connection = connectionFactory.createConnection();
        byte count = 1;
        while (count++ <= CONCURRENCY) {
            init(connection, delayQueue);
        }
    }

    /**
     * 初始化
     *
     * @param connection connection
     * @param delayQueue delayQueue
     */
    private void init(Connection connection, Queue delayQueue) {
        try {
            Channel channel = connection.createChannel(false);
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

            channel.basicConsume(delayQueue.getName(), true, defaultConsumer);
        } catch (Exception e) {
            logger.error("DelayMessageListener.initRabbitMq", e);
        }
    }

}
