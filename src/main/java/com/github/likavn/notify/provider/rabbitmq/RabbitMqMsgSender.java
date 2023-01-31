package com.github.likavn.notify.provider.rabbitmq;

import com.github.likavn.notify.api.DelayMsgListener;
import com.github.likavn.notify.base.DefaultMsgSender;
import com.github.likavn.notify.domain.MetaRequest;
import com.github.likavn.notify.provider.rabbitmq.config.NotifyRabbitMqConfig;
import com.github.likavn.notify.provider.rabbitmq.constant.RabbitMqConstant;
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
public class RabbitMqMsgSender extends DefaultMsgSender {
    private static final Logger logger = LoggerFactory.getLogger(RabbitMqMsgSender.class);
    /**
     * 消息过期时间，避免消息未消费导致消息堆积
     */
    private static final long MSG_EXPIRE = 30;

    private final RabbitTemplate rabbitTemplate;

    public RabbitMqMsgSender(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void send(String serviceId, String code, Object body) {
        MetaRequest<?> request = before(serviceId, code, body);
        //构建回调返回的数据 可做其他业务处理
        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
        rabbitTemplate.convertAndSend(RabbitMqConstant.EXCHANGE,
                String.format(RabbitMqConstant.ROUTING, request.getServiceId(), request.getCode()),
                WrapUtils.toJson(request),
                message -> {
                    message.getMessageProperties().setExpiration(MSG_EXPIRE + "");
                    message.getMessageProperties().setContentEncoding("utf-8");
                    return message;
                }, correlationData);
    }

    @Override
    @SuppressWarnings("all")
    public void sendDelayMessage(Class<? extends DelayMsgListener> handler, String code, Object body, Integer deliverNumber, long delayTime) {
        MetaRequest<?> request = before(handler, code, body, deliverNumber);
        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
        logger.debug("发送消息id={}", correlationData.getId());
        rabbitTemplate.convertAndSend(
                NotifyRabbitMqConfig.getDelayExchangeName(),
                NotifyRabbitMqConfig.getDelayRoutingName(),
                WrapUtils.toJson(request),
                message -> {
                    //配置消息的过期时间
                    message.getMessageProperties().setHeader("x-delay", delayTime <= 0 ? 2000 : delayTime * 1000);
                    return message;
                }, correlationData
        );
    }
}
