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
package com.github.likavn.eventbus.provider.redis;

import com.github.likavn.eventbus.core.DeliveryBus;
import com.github.likavn.eventbus.core.ListenerRegistry;
import com.github.likavn.eventbus.prop.BusProperties;
import com.github.likavn.eventbus.provider.redis.support.AbstractStreamListenerContainer;
import com.github.likavn.eventbus.provider.redis.support.RedisListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.Record;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

/**
 * redis消息监听器
 *
 * @author likavn
 * @since 2023/01/01
 **/
@Slf4j
public class RedisMsgSubscribeListener extends AbstractStreamListenerContainer {
    private final ListenerRegistry registry;
    private final DeliveryBus deliveryBus;

    public RedisMsgSubscribeListener(StringRedisTemplate stringRedisTemplate,
                                     BusProperties busProperties,
                                     ListenerRegistry registry,
                                     DeliveryBus deliveryBus) {
        super(stringRedisTemplate, busProperties);
        this.registry = registry;
        this.deliveryBus = deliveryBus;
    }

    @Override
    protected List<RedisListener> getListeners() {
        return RedisListener.timelyListeners(registry.getTimelyListeners());
    }

    @Override
    protected void deliver(RedisListener listener, Record<String, String> msg) {
        deliveryBus.deliverTimely(listener, msg.getValue());
    }
}
