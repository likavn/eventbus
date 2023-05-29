package com.github.likavn.notify.provider.rabbitmq;

import com.github.likavn.notify.base.AbstractMsgSender;
import com.github.likavn.notify.domain.Request;
import com.github.likavn.notify.provider.rabbitmq.constant.RabbitMqConstant;
import com.github.likavn.notify.utils.SpringUtil;
import com.github.likavn.notify.utils.WrapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.UUID;

/**
 * 通知生产者
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
    public void send(Request<?> request) {
        request.setIsOrgSub(Boolean.TRUE);
        request = wrap(request);
        //构建回调返回的数据 可做其他业务处理
        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
        rabbitTemplate.convertAndSend(RabbitMqConstant.EXCHANGE,
                String.format(RabbitMqConstant.ROUTING, request.getTopic()),
                WrapUtils.toJson(request),
                message -> {
                    message.getMessageProperties().setContentEncoding("utf-8");
                    return message;
                }, correlationData);
    }

    @Override
    @SuppressWarnings("all")
    public void sendDelayMessage(Request<?> request) {
        request = wrap(request);
        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
        logger.debug("发送消息id={}", correlationData.getId());
        long x_delay = 1000L * request.getDelayTime();
        rabbitTemplate.convertAndSend(
                String.format(RabbitMqConstant.DELAY_EXCHANGE, SpringUtil.getServiceId()),
                String.format(RabbitMqConstant.DELAY_ROUTING_KEY, SpringUtil.getServiceId()),
                WrapUtils.toJson(request),
                message -> {
                    //配置消息的过期时间
                    message.getMessageProperties().setHeader("x-delay", x_delay);
                    return message;
                }, correlationData
        );
    }
}
