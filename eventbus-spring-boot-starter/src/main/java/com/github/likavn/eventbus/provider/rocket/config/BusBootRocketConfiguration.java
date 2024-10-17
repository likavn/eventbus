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
package com.github.likavn.eventbus.provider.rocket.config;

import com.github.likavn.eventbus.ConditionalOnEventbusActive;
import com.github.likavn.eventbus.config.EventBusAutoConfiguration;
import com.github.likavn.eventbus.core.DeliveryBus;
import com.github.likavn.eventbus.core.ListenerRegistry;
import com.github.likavn.eventbus.core.api.RequestIdGenerator;
import com.github.likavn.eventbus.core.base.InterceptorContainer;
import com.github.likavn.eventbus.core.metadata.BusConfig;
import com.github.likavn.eventbus.core.metadata.BusType;
import com.github.likavn.eventbus.provider.rocket.RocketMsgSender;
import com.github.likavn.eventbus.provider.rocket.RocketMsgSubscribeListener;
import com.github.likavn.eventbus.provider.rocket.RocketNodeTestConnect;
import org.apache.rocketmq.spring.autoconfigure.RocketMQProperties;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * rocketmq实现配置
 *
 * @author likavn
 * @date 2024/01/01
 * @since 2.2
 */
@Configuration
@AutoConfigureAfter(EventBusAutoConfiguration.class)
@ConditionalOnEventbusActive(value = BusType.ROCKETMQ)
public class BusBootRocketConfiguration {
    @Bean
    @ConditionalOnMissingBean(RocketMsgSubscribeListener.class)
    public RocketMsgSubscribeListener rocketMsgSubscribeListener(
            RocketMQProperties properties, BusConfig busConfig, DeliveryBus deliveryBus, ListenerRegistry registry) {
        return new RocketMsgSubscribeListener(properties, busConfig, deliveryBus, registry);
    }

    @Configuration
    @ConditionalOnEventbusActive(value = BusType.ROCKETMQ, sender = true)
    static class RocketSenderConfiguration {

        @Bean
        @ConditionalOnMissingBean(RocketMsgSender.class)
        public RocketMsgSender msgSender(RocketMQTemplate template, BusConfig busConfig,
                                         @Lazy InterceptorContainer interceptorContainer,
                                         RequestIdGenerator requestIdGenerator, @Lazy ListenerRegistry registry) {
            return new RocketMsgSender(template, busConfig, interceptorContainer, requestIdGenerator, registry);
        }

        @Bean
        @ConditionalOnMissingBean(RocketNodeTestConnect.class)
        public RocketNodeTestConnect rocketNodeTestConnect(RocketMQProperties properties) {
            return new RocketNodeTestConnect(properties);
        }
    }
}
