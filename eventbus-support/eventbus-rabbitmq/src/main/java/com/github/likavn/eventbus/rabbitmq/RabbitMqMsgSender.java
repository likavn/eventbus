package com.github.likavn.eventbus.rabbitmq;

import com.github.likavn.eventbus.core.api.interceptor.SendAfterInterceptor;
import com.github.likavn.eventbus.core.api.interceptor.SendBeforeInterceptor;
import com.github.likavn.eventbus.core.base.AbstractSenderAdapter;
import com.github.likavn.eventbus.core.base.MsgListenerContainer;
import com.github.likavn.eventbus.core.metadata.BusConfig;
import com.github.likavn.eventbus.core.metadata.data.Request;
import com.github.likavn.eventbus.core.utils.Func;
import com.github.likavn.eventbus.rabbitmq.constant.RabbitConstant;
import com.github.likavn.eventbus.rabbitmq.support.ConnectPool;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * rabbitMq生产者
 *
 * @author likavn
 * @since 2023/01/01
 */
@Slf4j
public class RabbitMqMsgSender extends AbstractSenderAdapter implements MsgListenerContainer {
    private final ConnectionFactory connectionFactory;
    private final BusConfig config;
    private ConnectPool connectPool = null;

    public RabbitMqMsgSender(ConnectionFactory connectionFactory, SendBeforeInterceptor beforeInterceptor,
                             SendAfterInterceptor afterInterceptor,
                             BusConfig config) {
        super(beforeInterceptor, afterInterceptor, config);
        this.config = config;
        this.connectionFactory = connectionFactory;
    }

    @Override
    public void register() {
        try {
            this.connectPool = new ConnectPool(connectionFactory);
            this.channel = connection.createChannel();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void toSend(Request<?> request) {
        try {
            this.channel.basicPublish(RabbitConstant.EXCHANGE,
                    String.format(RabbitConstant.ROUTING, request.getTopic()), null, Func.toJson(request).getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void toSendDelayMessage(Request<?> request) {
        Map<String, Object> headers = new HashMap<>(2);
        headers.put("x-delay", 1000L * request.getDelayTime());
        try {
            this.channel.basicPublish(String.format(RabbitConstant.DELAY_EXCHANGE, config.getServiceId()),
                    String.format(RabbitConstant.DELAY_ROUTING_KEY, config.getServiceId()),
                    new AMQP.BasicProperties.Builder()
                            .headers(headers)
                            .build(), Func.toJson(request).getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void destroy() {
        try {
            this.channel.close();
        } catch (Exception e) {
            log.error("", e);
        }
        try {
            this.connection.close();
        } catch (IOException e) {
            log.error("", e);
        }
    }
}
