package com.github.likavn.eventbus.provider.rabbit.config;

import com.github.likavn.eventbus.core.DeliveryBus;
import com.github.likavn.eventbus.core.SubscriberRegistry;
import com.github.likavn.eventbus.core.api.MsgSender;
import com.github.likavn.eventbus.core.metadata.BusConfig;
import com.github.likavn.eventbus.core.metadata.InterceptorConfig;
import com.github.likavn.eventbus.provider.rabbit.RabbitMsgDelayListener;
import com.github.likavn.eventbus.provider.rabbit.RabbitMsgSender;
import com.github.likavn.eventbus.provider.rabbit.RabbitMsgSubscribeListener;
import com.github.likavn.eventbus.provider.rabbit.RabbitNodeTestConnect;
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
 * @date 2024/01/01
 */
@Configuration
@ConditionalOnClass(RabbitTemplate.class)
@ConditionalOnProperty(prefix = "eventbus", name = "type", havingValue = "rabbitmq")
public class BusBootRabbitConfig {

    @Bean
    public MsgSender msgSender(InterceptorConfig interceptorConfig, BusConfig config, RabbitTemplate rabbitTemplate) {
        return new RabbitMsgSender(interceptorConfig, config, rabbitTemplate);
    }

    @Bean
    public RabbitMsgSubscribeListener rabbitMsgSubscribeListener(BusConfig config, SubscriberRegistry registry, DeliveryBus deliveryBus, CachingConnectionFactory connectionFactory) {
        return new RabbitMsgSubscribeListener(config, deliveryBus, registry.getSubscribers(), connectionFactory);
    }

    @Bean
    public RabbitMsgDelayListener rabbitMsgDelayListener(BusConfig config, DeliveryBus deliveryBus, CachingConnectionFactory connectionFactory) {
        return new RabbitMsgDelayListener(config, deliveryBus, connectionFactory);
    }

    @Bean
    public RabbitNodeTestConnect redisNodeTestConnect(BusConfig config, CachingConnectionFactory connectionFactory) {
        return new RabbitNodeTestConnect(config, connectionFactory);
    }
}
