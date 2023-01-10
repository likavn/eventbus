package com.github.likavn.notify.provider.rabbitmq;

import com.github.likavn.notify.base.BaseDelayMsgListener;
import com.github.likavn.notify.utils.WrapUtils;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.Connection;

/**
 * 延时消息处理类
 *
 * @author likavn
 * @since 2023/01/01
 */
@Slf4j
public class RabbitMqDelayMsgListener extends BaseDelayMsgListener {
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
                        handler(WrapUtils.convertByBytes(body));
                    } catch (Exception ex) {
                        log.error("DelayMessageListener", ex);
                    }
                }
            };

            channel.basicConsume(delayQueue.getName(), true, defaultConsumer);
        } catch (Exception e) {
            log.error("DelayMessageListener.initRabbitMq", e);
        }
    }

}
