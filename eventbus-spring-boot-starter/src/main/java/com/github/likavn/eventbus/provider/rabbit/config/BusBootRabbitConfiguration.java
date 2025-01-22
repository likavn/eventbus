/**
 * Copyright 2023-2033, likavn (likavn@163.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.likavn.eventbus.provider.rabbit.config;

import com.github.likavn.eventbus.ConditionalOnEventbusActive;
import com.github.likavn.eventbus.core.DeliveryBus;
import com.github.likavn.eventbus.core.ListenerRegistry;
import com.github.likavn.eventbus.core.api.RequestIdGenerator;
import com.github.likavn.eventbus.core.base.InterceptorContainer;
import com.github.likavn.eventbus.core.metadata.BusConfig;
import com.github.likavn.eventbus.core.metadata.BusType;
import com.github.likavn.eventbus.provider.rabbit.RabbitMsgSender;
import com.github.likavn.eventbus.provider.rabbit.RabbitMsgSubscribeListener;
import com.github.likavn.eventbus.provider.rabbit.RabbitNodeTestConnect;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * rabbitMq实现配置
 *
 * @author likavn
 * @date 2024/01/01
 */
@Configuration
@ConditionalOnEventbusActive(value = BusType.RABBITMQ)
public class BusBootRabbitConfiguration {

    @Bean
    @ConditionalOnMissingBean(RabbitMsgSubscribeListener.class)
    public RabbitMsgSubscribeListener rabbitMsgSubscribeListener(
            CachingConnectionFactory connectionFactory, BusConfig config, DeliveryBus deliveryBus, ListenerRegistry registry) {
        return new RabbitMsgSubscribeListener(connectionFactory, config, deliveryBus, registry);
    }

    @Configuration
    @ConditionalOnEventbusActive(value = BusType.RABBITMQ, sender = true)
    static class RabbitSenderConfiguration {

        @Bean
        @ConditionalOnMissingBean(RabbitMsgSender.class)
        public RabbitMsgSender msgSender(RabbitTemplate rabbitTemplate,
                                         BusConfig config,
                                         @Lazy InterceptorContainer interceptorContainer,
                                         RequestIdGenerator requestIdGenerator,
                                         @Lazy ListenerRegistry registry) {
            return new RabbitMsgSender(rabbitTemplate, config, interceptorContainer, requestIdGenerator, registry);
        }

        @Bean
        @ConditionalOnMissingBean(RabbitNodeTestConnect.class)
        public RabbitNodeTestConnect rabbitNodeTestConnect(CachingConnectionFactory connectionFactory) {
            return new RabbitNodeTestConnect(connectionFactory);
        }
    }
}
