package com.github.likavn.eventbus.provider.rabbit;

import com.github.likavn.eventbus.core.DeliveryBus;
import com.github.likavn.eventbus.core.SubscriberRegistry;
import com.github.likavn.eventbus.core.api.MsgSender;
import com.github.likavn.eventbus.core.metadata.BusConfig;
import com.github.likavn.eventbus.core.metadata.InterceptorConfig;
import com.github.likavn.eventbus.rabbitmq.RabbitMsgDelayListener;
import com.github.likavn.eventbus.rabbitmq.RabbitMsgSender;
import com.github.likavn.eventbus.rabbitmq.RabbitMsgSubscribeListener;
import com.github.likavn.eventbus.rabbitmq.RabbitNodeTestConnect;
import com.rabbitmq.client.ConnectionFactory;
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
    public ConnectionFactory connectionFactory() {
        return new ConnectionFactory();
    }

    @Bean
    public MsgSender msgSender(ConnectionFactory connectionFactory, BusConfig busConfig, InterceptorConfig interceptorConfig) {
        return new RabbitMsgSender(connectionFactory, interceptorConfig, busConfig);
    }

    @Bean
    public RabbitMsgSubscribeListener rabbitMsgSubscribeListener(
            ConnectionFactory connectionFactory, DeliveryBus deliveryBus, BusConfig config, SubscriberRegistry registry) {
        return new RabbitMsgSubscribeListener(connectionFactory, deliveryBus, config, registry.getSubscribers());
    }

    @Bean
    public RabbitMsgDelayListener rabbitMsgDelayListener(ConnectionFactory connectionFactory, DeliveryBus deliveryBus, BusConfig config) {
        return new RabbitMsgDelayListener(connectionFactory, config, deliveryBus);
    }

    @Bean
    public RabbitNodeTestConnect redisNodeTestConnect(ConnectionFactory connectionFactory, BusConfig config) {
        return new RabbitNodeTestConnect(connectionFactory, config);
    }
}
