package com.github.likavn.eventbus.provider.rabbit;

import com.github.likavn.eventbus.core.base.Lifecycle;
import com.github.likavn.eventbus.core.base.NodeTestConnect;
import com.github.likavn.eventbus.core.metadata.BusConfig;
import com.github.likavn.eventbus.provider.rabbit.constant.RabbitConstant;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.Connection;

import java.io.IOException;

/**
 * rabbitMq连接状态测试
 *
 * @author likavn
 * @date 2023/5/30
 **/
@Slf4j
public class RabbitNodeTestConnect implements NodeTestConnect, Lifecycle {
    private final CachingConnectionFactory connectionFactory;
    private final String queueName;
    private Connection connection;
    private Channel channel;

    public RabbitNodeTestConnect(BusConfig config, CachingConnectionFactory connectionFactory) {
        this.queueName = String.format(RabbitConstant.QUEUE_DELAY, config.getServiceId());
        this.connectionFactory = connectionFactory;
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

    @Override
    public void register() throws Exception {

    }

    @Override
    public void destroy() throws Exception {
        if (null != this.channel) {
            this.channel.close();
            this.channel = null;
        }
        if (null != this.connection) {
            this.connection.close();
            this.connection = null;
        }
    }
}
