package com.github.likavn.eventbus.rabbitmq;

import com.github.likavn.eventbus.core.base.MsgListenerContainer;
import com.github.likavn.eventbus.core.base.NodeTestConnect;
import com.github.likavn.eventbus.core.metadata.BusConfig;
import com.github.likavn.eventbus.rabbitmq.constant.RabbitConstant;
import com.github.likavn.eventbus.rabbitmq.support.Connect;
import com.rabbitmq.client.Channel;
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
public class RabbitMqNodeTestConnect implements NodeTestConnect, MsgListenerContainer {
    private final ConnectionFactory connectionFactory;
    private final String queueName;
    private Connect connect = null;
    private Channel channel = null;

    public RabbitMqNodeTestConnect(ConnectionFactory connectionFactory, BusConfig config) {
        this.connectionFactory = connectionFactory;
        this.queueName = String.format(RabbitConstant.DELAY_QUEUE, config.getServiceId());
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
        this.connect = new Connect(connectionFactory);
        this.channel = this.connect.createChannel();
    }

    @Override
    public void destroy() throws IOException {
        if (null != this.connect) {
            this.connect.close();
        }
    }
}
