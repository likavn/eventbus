package com.github.likavn.notify.provider.rabbitmq;

import com.github.likavn.notify.base.NodeTestConnect;
import com.github.likavn.notify.provider.rabbitmq.constant.RabbitMqConstant;
import com.github.likavn.notify.utils.SpringUtil;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.Connection;

import java.io.IOException;

/**
 * @author likavn
 * @date 2023/5/30
 **/
@Slf4j
public class RabbitMqNodeTestConnect implements NodeTestConnect {
    private final CachingConnectionFactory connectionFactory;

    private final String queueName;

    private Connection connection;

    private Channel channel;

    public RabbitMqNodeTestConnect(CachingConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
        this.queueName = String.format(RabbitMqConstant.DELAY_QUEUE, SpringUtil.getServiceId());
    }

    @Override
    public synchronized boolean testConnect() {
        try {
            if (null == connection) {
                connection = connectionFactory.createConnection();
            }

            if (null == channel) {
                channel = connection.createChannel(false);
            }
            channel.consumerCount(queueName);
            return true;
        } catch (AmqpConnectException | IOException ex) {
            connection = null;
            channel = null;
            log.error("rabbitMq timeout", ex);
            return false;
        }
    }

}
