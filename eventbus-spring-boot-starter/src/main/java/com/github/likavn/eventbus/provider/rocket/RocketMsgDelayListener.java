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
import com.github.likavn.eventbus.core.metadata.BusConfig;
import com.github.likavn.eventbus.core.metadata.support.Listener;
import com.github.likavn.eventbus.provider.rocket.support.AbstractRocketRegisterContainer;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.autoconfigure.RocketMQProperties;

import java.util.Collections;
import java.util.List;

/**
 * 延时消息监听处理器
 *
 * @author likavn
 * @since 2023/01/01
 * @since 2.2
 */
public class RocketMsgDelayListener extends AbstractRocketRegisterContainer {
    private final DeliveryBus deliveryBus;

    public RocketMsgDelayListener(RocketMQProperties rocketMqProperties,
                                  BusConfig config,
                                  DeliveryBus deliveryBus) {
        super(rocketMqProperties, config);
        this.deliveryBus = deliveryBus;
    }

    @Override
    public List<Listener> getListeners() {
        return Collections.singletonList(Listener.ofDelay(config));
    }

    @Override
    protected void deliver(Listener subscriber, MessageExt msg) {
        deliveryBus.deliverDelay(msg.getBody());
    }
}
