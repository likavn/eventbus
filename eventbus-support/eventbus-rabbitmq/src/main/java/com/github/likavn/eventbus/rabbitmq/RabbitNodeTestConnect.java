package com.github.likavn.eventbus.rabbitmq;

import com.github.likavn.eventbus.core.base.Lifecycle;
import com.github.likavn.eventbus.core.base.NodeTestConnect;
import com.github.likavn.eventbus.core.metadata.BusConfig;
import com.github.likavn.eventbus.rabbitmq.constant.RabbitConstant;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * rabbitMq连接状态测试
 *
 * @author likavn
 * @date 2023/5/30
 **/
@Slf4j
public class RabbitNodeTestConnect implements NodeTestConnect, Lifecycle {
    private final ConnectionFactory connectionFactory;
    private final String queueName;
    private Connection connection = null;
    private Channel channel = null;

    public RabbitNodeTestConnect(ConnectionFactory connectionFactory, BusConfig config) {
        this.connectionFactory = connectionFactory;
        this.queueName = String.format(RabbitConstant.QUEUE_DELAY, config.getServiceId());
    }

    @Override
    public synchronized boolean testConnect() {
        try {
            this.channel.consumerCount(queueName);
            return true;
        } catch (IOException ex) {
            log.error("rabbitMq testConnect", ex);
            return false;
        }
    }

    @Override
    public void register() throws IOException, TimeoutException {
        this.connection = connectionFactory.newConnection();
        this.channel = this.connection.createChannel();
    }

    @Override
    public void destroy() throws IOException, TimeoutException {
        if (null != this.channel) {
            this.channel.close();
        }
    }
}
