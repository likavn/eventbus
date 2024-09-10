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

import com.github.likavn.eventbus.core.ListenerRegistry;
import com.github.likavn.eventbus.core.metadata.support.Listener;
import com.github.likavn.eventbus.core.utils.Func;
import com.github.likavn.eventbus.provider.redis.constant.RedisConstant;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

/**
 * redis消息订阅监听器消费者实体数据
 *
 * @author likavn
 * @date 2023/1/7
 **/
@Data
@SuppressWarnings("all")
public class RedisListener extends Listener {

    /**
     * 消费者监听stream key
     */
    private final String streamKey;

    /**
     * 消费者所在消费者组
     */
    private final String group;
    /**
     * 消息编码
     */
    private String code;
    private String topic;

    public RedisListener(Listener listener, String code, String subscribePrefix) {
        super(listener.getServiceId(),
                listener.getCodes(),
                listener.getConcurrency(),
                listener.getTrigger(), listener.getFailTrigger(), listener.getPolling());
        setType(listener.getType());
        this.code = code;
        this.topic = Func.getTopic(listener.getServiceId(), code);
        this.streamKey = String.format(subscribePrefix, topic);
        this.group = listener.getDeliverId();
    }

    public String getStreamKey() {
        return streamKey;
    }

    public String getGroup() {
        return group;
    }

    public static List<RedisListener> fullRedisListeners(ListenerRegistry registry) {
        List<RedisListener> listeners = timelyListeners(registry.getTimelyListeners());
        // 延时的消息订阅
        listeners.addAll(delayListeners(registry.getDelayListeners()));
        return listeners;
    }

    public static List<RedisListener> timelyListeners(List<Listener> listeners) {
        return listeners.stream().flatMap(t -> {
            List<String> codes = t.getCodes();
            return codes.stream().map(code -> new RedisListener(t, code, RedisConstant.BUS_SUBSCRIBE_PREFIX));
        }).collect(Collectors.toList());
    }

    public static List<RedisListener> redisFullDelayListeners(ListenerRegistry registry) {
        return delayListeners(registry.getFullListeners());
    }

    public static List<RedisListener> delayListeners(List<Listener> listeners) {
        return listeners.stream().flatMap(t -> {
            List<String> codes = t.getCodes();
            return codes.stream().map(code -> new RedisListener(t, code, RedisConstant.BUS_DELAY_SUBSCRIBE_PREFIX));
        }).collect(Collectors.toList());
    }
}
