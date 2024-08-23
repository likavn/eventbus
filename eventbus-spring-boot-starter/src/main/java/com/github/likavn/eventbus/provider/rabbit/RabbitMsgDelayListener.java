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
package com.github.likavn.eventbus.provider.rabbit;

import com.github.likavn.eventbus.core.DeliveryBus;
import com.github.likavn.eventbus.core.ListenerRegistry;
import com.github.likavn.eventbus.core.metadata.BusConfig;
import com.github.likavn.eventbus.core.metadata.MsgType;
import com.github.likavn.eventbus.core.metadata.support.Listener;
import com.github.likavn.eventbus.provider.rabbit.support.AbstractRabbitRegisterContainer;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;

import java.util.List;

/**
 * 延时消息监听处理器
 *
 * @author likavn
 * @since 2023/01/01
 */
public class RabbitMsgDelayListener extends AbstractRabbitRegisterContainer {
    private final DeliveryBus deliveryBus;
    private final ListenerRegistry registry;

    public RabbitMsgDelayListener(CachingConnectionFactory connectionFactory,
                                  BusConfig config,
                                  DeliveryBus deliveryBus,
                                  ListenerRegistry registry) {
        super(connectionFactory, config, MsgType.DELAY);
        this.deliveryBus = deliveryBus;
        this.registry = registry;
    }

    @Override
    public List<Listener> getListeners() {
        List<Listener> listeners = registry.getFullListeners();
        // 兼容之前版本的延时消息
        listeners.add(Listener.ofDelay(config));
        return listeners;
    }

    @Override
    protected void deliver(Listener listener, byte[] body) {
        deliveryBus.deliverDelay(listener, body);
    }
}
