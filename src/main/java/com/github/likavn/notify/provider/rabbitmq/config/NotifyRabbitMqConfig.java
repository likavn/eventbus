package com.github.likavn.notify.provider.rabbitmq.config;

import com.github.likavn.notify.api.MsgSender;
import com.github.likavn.notify.domain.ServiceContext;
import com.github.likavn.notify.prop.NotifyProperties;
import com.github.likavn.notify.provider.rabbitmq.RabbitMqDelayMsgListener;
import com.github.likavn.notify.provider.rabbitmq.RabbitMqMsgSender;
import com.github.likavn.notify.provider.rabbitmq.RabbitMqNodeTestConnect;
import com.github.likavn.notify.provider.rabbitmq.RabbitMqSubscribeMsgListener;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * rabbitMq实现配置
 *
 * @author likavn
 * @since 2023/01/01
 */
@Configuration
@SuppressWarnings("all")
@ConditionalOnClass(RabbitTemplate.class)
@ConditionalOnProperty(prefix = "notify", name = "type", havingValue = "rabbitmq")
public class NotifyRabbitMqConfig {

    @Bean
    public MsgSender rabbitsMqMsgSender(RabbitTemplate rabbitTemplate) {
        return new RabbitMqMsgSender(rabbitTemplate);
    }

    @Bean
    public RabbitMqSubscribeMsgListener rabbitMqSubscribeMsgListener(
            CachingConnectionFactory connectionFactory, ServiceContext serviceContext, NotifyProperties notifyProperties) {
        return new RabbitMqSubscribeMsgListener(serviceContext.getSubMsgConsumers(), connectionFactory, notifyProperties);
    }

    @Bean
    public RabbitMqDelayMsgListener delayMessageListener(CachingConnectionFactory connectionFactory, NotifyProperties notifyProperties) {
        return new RabbitMqDelayMsgListener(connectionFactory, notifyProperties);
    }

    @Bean
    public RabbitMqNodeTestConnect rabbitMqNodeTestConnect(CachingConnectionFactory connectionFactory) {
        return new RabbitMqNodeTestConnect(connectionFactory);
    }
}
