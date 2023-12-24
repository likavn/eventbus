package com.github.likavn.eventbus.rabbitmq;

import com.github.likavn.eventbus.core.api.interceptor.SendAfterInterceptor;
import com.github.likavn.eventbus.core.api.interceptor.SendBeforeInterceptor;
import com.github.likavn.eventbus.core.base.AbstractSenderAdapter;
import com.github.likavn.eventbus.core.base.Lifecycle;
import com.github.likavn.eventbus.core.metadata.BusConfig;
import com.github.likavn.eventbus.core.metadata.data.Request;
import com.github.likavn.eventbus.core.utils.Func;
import com.github.likavn.eventbus.rabbitmq.constant.RabbitConstant;
import com.github.likavn.eventbus.rabbitmq.support.AmqpException;
import com.github.likavn.eventbus.rabbitmq.support.pool.ChannelPool;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * rabbitMq生产者
 *
 * @author likavn
 * @since 2023/01/01
 */
@Slf4j
public class RabbitMsgSender extends AbstractSenderAdapter implements Lifecycle {
    private final ConnectionFactory connectionFactory;
    private final BusConfig config;
    private ChannelPool channelPool = null;

    public RabbitMsgSender(ConnectionFactory connectionFactory, SendBeforeInterceptor beforeInterceptor,
                           SendAfterInterceptor afterInterceptor,
                           BusConfig config) {
        super(beforeInterceptor, afterInterceptor, config);
        this.config = config;
        this.connectionFactory = connectionFactory;
    }

    @Override
    public void register() throws IOException, TimeoutException {
        this.channelPool = new ChannelPool(connectionFactory);
    }

    @Override
    public void toSend(Request<?> request) {
        Channel channel = this.channelPool.borrowObject();
        try {
            channel.basicPublish(RabbitConstant.EXCHANGE,
                    String.format(RabbitConstant.ROUTING, request.getTopic()), null, Func.toJson(request).getBytes());
        } catch (IOException e) {
            throw new AmqpException(e);
        } finally {
            this.channelPool.returnObject(channel);
        }
    }

    @Override
    public void toSendDelayMessage(Request<?> request) {
        Channel channel = this.channelPool.borrowObject();
        try {
            Map<String, Object> headers = new HashMap<>(2);
            headers.put("x-delay", 1000L * request.getDelayTime());
            channel.basicPublish(String.format(RabbitConstant.EXCHANGE_DELAY, config.getServiceId()),
                    String.format(RabbitConstant.ROUTING_KEY_DELAY, config.getServiceId()),
                    new AMQP.BasicProperties.Builder()
                            .headers(headers)
                            .build(), Func.toJson(request).getBytes());
        } catch (IOException e) {
            throw new AmqpException(e);
        } finally {
            this.channelPool.returnObject(channel);
        }
    }

    @Override
    public void destroy() throws IOException {
        if (null != this.channelPool.borrowObject()) {
            this.channelPool.close();
        }
    }
}
