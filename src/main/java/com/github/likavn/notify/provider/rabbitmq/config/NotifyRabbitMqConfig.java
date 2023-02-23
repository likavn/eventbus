package com.github.likavn.notify.provider.rabbitmq.config;

import com.github.likavn.notify.api.MsgSender;
import com.github.likavn.notify.domain.MetaServiceProperty;
import com.github.likavn.notify.provider.rabbitmq.RabbitMqDelayMsgListener;
import com.github.likavn.notify.provider.rabbitmq.RabbitMqMsgSender;
import com.github.likavn.notify.provider.rabbitmq.RabbitMqSubscribeMsgListener;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 通知配置
 *
 * @author likavn
 * @since 2023/01/01
 */
@Configuration
@SuppressWarnings("all")
@ConditionalOnClass(RabbitTemplate.class)
@ConditionalOnProperty(prefix = "notify", name = "type", havingValue = "rabbitmq")
public class NotifyRabbitMqConfig {

    /**
     * 消息通知rabbitmq实现
     */
    @Bean
    public MsgSender rabbitsMqMsgSender(RabbitTemplate rabbitTemplate) {
        return new RabbitMqMsgSender(rabbitTemplate);
    }

    @Bean
    public RabbitMqSubscribeMsgListener rabbitMqSubscribeMsgListener(
            CachingConnectionFactory connectionFactory, MetaServiceProperty serviceProperty) {
        return new RabbitMqSubscribeMsgListener(serviceProperty.getSubMsgConsumers(), connectionFactory);
    }

    /**
     * 初始化延时事件消息监听器
     */
    @Bean
    public RabbitMqDelayMsgListener delayMessageListener(CachingConnectionFactory connectionFactory) {
        return new RabbitMqDelayMsgListener(connectionFactory);
    }
}
