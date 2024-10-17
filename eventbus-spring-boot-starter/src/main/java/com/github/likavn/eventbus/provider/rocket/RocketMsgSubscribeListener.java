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
package com.github.likavn.eventbus.provider.rocket;

import com.github.likavn.eventbus.core.DeliveryBus;
import com.github.likavn.eventbus.core.ListenerRegistry;
import com.github.likavn.eventbus.core.metadata.BusConfig;
import com.github.likavn.eventbus.core.metadata.support.Listener;
import com.github.likavn.eventbus.provider.rocket.support.AbstractRocketRegisterContainer;
import com.github.likavn.eventbus.provider.rocket.support.RocketListener;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.autoconfigure.RocketMQProperties;

import java.util.List;

/**
 * rabbitMq消息订阅器
 *
 * @author likavn
 * @date 2023/01/01
 * @since 2.2
 **/
public class RocketMsgSubscribeListener extends AbstractRocketRegisterContainer {
    private final DeliveryBus deliveryBus;
    private final ListenerRegistry registry;

    public RocketMsgSubscribeListener(RocketMQProperties rocketMqProperties,
                                      BusConfig config, DeliveryBus deliveryBus, ListenerRegistry registry) {
        super(rocketMqProperties, config);
        this.deliveryBus = deliveryBus;
        this.registry = registry;
    }

    @Override
    public List<RocketListener> getListeners() {
        return RocketListener.getAllListeners(registry);
    }

    @Override
    protected void deliver(Listener subscriber, MessageExt msg) {
        deliveryBus.deliver(subscriber, msg.getBody());
    }
}
