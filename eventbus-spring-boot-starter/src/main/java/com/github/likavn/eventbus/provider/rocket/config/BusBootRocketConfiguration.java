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

import com.github.likavn.eventbus.core.DeliveryBus;
import com.github.likavn.eventbus.core.ListenerRegistry;
import com.github.likavn.eventbus.core.api.MsgSender;
import com.github.likavn.eventbus.core.api.RequestIdGenerator;
import com.github.likavn.eventbus.core.metadata.BusConfig;
import com.github.likavn.eventbus.core.metadata.InterceptorConfig;
import com.github.likavn.eventbus.provider.rocket.RocketMsgDelayListener;
import com.github.likavn.eventbus.provider.rocket.RocketMsgSender;
import com.github.likavn.eventbus.provider.rocket.RocketMsgSubscribeListener;
import com.github.likavn.eventbus.provider.rocket.RocketNodeTestConnect;
import org.apache.rocketmq.spring.autoconfigure.RocketMQProperties;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
@ConditionalOnProperty(prefix = "eventbus", name = "type", havingValue = "rocketmq")
public class BusBootRocketConfiguration {

    @Bean
    public MsgSender msgSender(RocketMQTemplate rocketMQTemplate, BusConfig busConfig,
                               @Lazy InterceptorConfig interceptorConfig, RequestIdGenerator requestIdGenerator, @Lazy ListenerRegistry registry) {
        return new RocketMsgSender(rocketMQTemplate, busConfig, interceptorConfig, requestIdGenerator, registry);
    }

    @Bean
    public RocketMsgSubscribeListener rocketMsgSubscribeListener(
            RocketMQProperties rocketMQProperties, BusConfig busConfig, DeliveryBus deliveryBus, ListenerRegistry registry) {
        return new RocketMsgSubscribeListener(rocketMQProperties, busConfig, deliveryBus, registry.getTimelyListeners());
    }

    @Bean
    public RocketMsgDelayListener rocketMsgDelayListener(
            RocketMQProperties rocketMQProperties, BusConfig busConfig, DeliveryBus deliveryBus, @Lazy ListenerRegistry registry) {
        return new RocketMsgDelayListener(rocketMQProperties, busConfig, deliveryBus, registry);
    }

    @Bean
    public RocketNodeTestConnect rocketNodeTestConnect(RocketMQProperties rocketMQProperties) {
        return new RocketNodeTestConnect(rocketMQProperties);
    }
}
