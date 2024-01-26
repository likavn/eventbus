package com.github.likavn.eventbus.provider.rabbit;

import com.github.likavn.eventbus.core.base.NodeTestConnect;
import com.github.likavn.eventbus.core.metadata.BusConfig;
import com.github.likavn.eventbus.provider.rabbit.constant.RabbitConstant;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.Connection;

/**
 * rabbitMq连接状态测试
 *
 * @author likavn
 * @date 2023/5/30
 **/
@Slf4j
public class RabbitNodeTestConnect implements NodeTestConnect {
    private final CachingConnectionFactory connectionFactory;
    private final String queueName;
    private Connection connection;
    private Channel channel;

    public RabbitNodeTestConnect(CachingConnectionFactory connectionFactory, BusConfig config) {
        this.connectionFactory = connectionFactory;
        this.queueName = String.format(RabbitConstant.DELAY_QUEUE, config.getServiceId());
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
        } catch (Exception ex) {
            log.error("rabbitMq testConnect", ex);
            return false;
        }
    }
}
