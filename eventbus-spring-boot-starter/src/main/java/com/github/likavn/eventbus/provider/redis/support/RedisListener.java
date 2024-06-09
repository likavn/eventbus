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
package com.github.likavn.eventbus.provider.redis.support;

import com.github.likavn.eventbus.core.metadata.MsgType;
import com.github.likavn.eventbus.core.metadata.support.Listener;
import com.github.likavn.eventbus.provider.redis.constant.RedisConstant;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * redis消息订阅监听器消费者实体数据
 *
 * @author likavn
 * @date 2023/1/7
 **/
@SuppressWarnings("all")
public final class RedisListener extends Listener {

    /**
     * 消费者监听stream key
     */
    private final String streamKey;

    /**
     * 消费者所在消费者组
     */
    private final String group;

    public RedisListener(Listener subscriber, String subscribePrefix) {
        super(subscriber.getServiceId(), subscriber.getCode(),
                subscriber.getConcurrency(), subscriber.getType(), subscriber.getTrigger(), subscriber.getFailTrigger());
        this.streamKey = String.format(subscribePrefix, subscriber.getTopic());
        this.group = null != subscriber.getTrigger() ? subscriber.getTrigger().getDeliverId() : subscriber.getServiceId();
    }

    public String getStreamKey() {
        return streamKey;
    }

    public String getGroup() {
        return group;
    }

    public static List<RedisListener> fullRedisSubscriber(List<Listener> listeners, String serviceId) {
        List<RedisListener> redisSubscribers = redisListeners(listeners);

        // 延时的消息订阅
        redisSubscribers.addAll(redisDelaySubscriber(serviceId));
        return redisSubscribers;
    }

    public static List<RedisListener> redisListeners(List<Listener> listeners) {
        return listeners.stream().map(t -> new RedisListener(t, RedisConstant.BUS_SUBSCRIBE_PREFIX)).collect(Collectors.toList());
    }

    public static List<RedisListener> redisDelaySubscriber(String serviceId) {
        return redisDelaySubscriber(serviceId, 1);
    }

    public static List<RedisListener> redisDelaySubscriber(String serviceId, int concurrency) {
        Listener listener = new Listener(serviceId, null, concurrency, MsgType.DELAY);
        return Collections.singletonList(new RedisListener(listener, RedisConstant.BUS_DELAY_SUBSCRIBE_PREFIX));
    }
}
