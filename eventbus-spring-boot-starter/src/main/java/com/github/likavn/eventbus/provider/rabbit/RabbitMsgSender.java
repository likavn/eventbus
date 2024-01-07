package com.github.likavn.eventbus.provider.rabbit;

import com.github.likavn.eventbus.core.base.AbstractSenderAdapter;
import com.github.likavn.eventbus.core.metadata.BusConfig;
import com.github.likavn.eventbus.core.metadata.InterceptorConfig;
import com.github.likavn.eventbus.core.metadata.data.Request;
import com.github.likavn.eventbus.core.utils.Func;
import com.github.likavn.eventbus.provider.rabbit.constant.RabbitConstant;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * rabbitMq生产者
 *
 * @author likavn
 * @since 2023/01/01
 */
public class RabbitMsgSender extends AbstractSenderAdapter {
    private final BusConfig config;
    private final RabbitTemplate rabbitTemplate;

    public RabbitMsgSender(InterceptorConfig interceptorConfig, BusConfig config, RabbitTemplate rabbitTemplate) {
        super(interceptorConfig, config);
        this.config = config;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void toSend(Request<?> request) {
        rabbitTemplate.convertAndSend(
                String.format(RabbitConstant.EXCHANGE, config.getServiceId()),
                String.format(RabbitConstant.ROUTING, request.getTopic()),
                Func.toJson(request),
                message -> {
                    message.getMessageProperties().setContentEncoding("utf-8");
                    return message;
                },
                new CorrelationData(request.getRequestId()));
    }

    @Override
    public void toSendDelayMessage(Request<?> request) {
        rabbitTemplate.convertAndSend(
                String.format(RabbitConstant.EXCHANGE_DELAY, config.getServiceId()),
                String.format(RabbitConstant.ROUTING_KEY_DELAY, config.getServiceId()),
                Func.toJson(request),
                message -> {
                    //配置消息的过期时间,单位：毫秒
                    message.getMessageProperties().setHeader("x-delay", 1000L * request.getDelayTime());
                    return message;
                },
                new CorrelationData(request.getRequestId())
        );
    }
}
