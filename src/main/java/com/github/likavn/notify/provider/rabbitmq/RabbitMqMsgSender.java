package com.github.likavn.notify.provider.rabbitmq;

import com.github.likavn.notify.base.DefaultMsgSender;
import com.github.likavn.notify.config.NotifyRabbitMqConfig;
import com.github.likavn.notify.constant.MsgConstant;
import com.github.likavn.notify.domain.MsgRequest;
import com.github.likavn.notify.utils.WrapUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.UUID;

/**
 * 通知生产者
 *
 * @author likavn
 * @since 2023/01/01
 */
@Slf4j
public class RabbitMqMsgSender extends DefaultMsgSender {
    /**
     * 消息过期时间，避免消息未消费导致消息堆积
     */
    private static final long MSG_EXPIRE = 30;

    private final RabbitTemplate rabbitTemplate;

    public RabbitMqMsgSender(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void send(MsgRequest<?> request) {
        before(request);
        //构建回调返回的数据 可做其他业务处理
        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
        rabbitTemplate.convertAndSend(MsgConstant.EXCHANGE,
                String.format(MsgConstant.ROUTING, request.getServiceId(), request.getCode()),
                WrapUtils.toJson(request),
                message -> {
                    message.getMessageProperties().setExpiration(MSG_EXPIRE + "");
                    message.getMessageProperties().setContentEncoding("utf-8");
                    return message;
                }, correlationData);
    }

    @Override
    public void sendDelayMessage(MsgRequest<?> request, long delayTime) {
        before(request);
        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
        log.info("发送消息id={}", correlationData.getId());
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
