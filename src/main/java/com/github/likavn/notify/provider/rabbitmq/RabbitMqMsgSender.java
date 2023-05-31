package com.github.likavn.notify.provider.rabbitmq;

import com.github.likavn.notify.base.AbstractMsgSender;
import com.github.likavn.notify.domain.Request;
import com.github.likavn.notify.provider.rabbitmq.constant.RabbitMqConstant;
import com.github.likavn.notify.utils.Func;
import com.github.likavn.notify.utils.SpringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.UUID;

/**
 * rabbitMq生产者
 *
 * @author likavn
 * @since 2023/01/01
 */
public class RabbitMqMsgSender extends AbstractMsgSender {
    private static final Logger logger = LoggerFactory.getLogger(RabbitMqMsgSender.class);

    private final RabbitTemplate rabbitTemplate;

    public RabbitMqMsgSender(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void toSend(Request<?> request) {
        rabbitTemplate.convertAndSend(RabbitMqConstant.EXCHANGE,
                String.format(RabbitMqConstant.ROUTING, request.getTopic()),
                Func.toJson(request),
                message -> {
                    message.getMessageProperties().setContentEncoding("utf-8");
                    return message;
                },
                new CorrelationData(UUID.randomUUID().toString()));
    }

    @Override
    public void toSendDelayMessage(Request<?> request) {
        rabbitTemplate.convertAndSend(
                String.format(RabbitMqConstant.DELAY_EXCHANGE, SpringUtil.getServiceId()),
                String.format(RabbitMqConstant.DELAY_ROUTING_KEY, SpringUtil.getServiceId()),
                Func.toJson(request),
                message -> {
                    //配置消息的过期时间,单位：毫秒
                    message.getMessageProperties().setHeader("x-delay", 1000L * request.getDelayTime());
                    return message;
                },
                new CorrelationData(UUID.randomUUID().toString())
        );
    }
}
