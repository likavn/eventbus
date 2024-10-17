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
import com.github.likavn.eventbus.core.metadata.MsgType;
import com.github.likavn.eventbus.core.metadata.support.Listener;
import com.github.likavn.eventbus.provider.SingleCodeListener;
import com.github.likavn.eventbus.provider.redis.constant.RedisConstant;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

import static com.github.likavn.eventbus.provider.SingleCodeListener.FieldExpression.*;

/**
 * redis消息订阅监听器消费者实体数据
 *
 * @author likavn
 * @date 2023/1/7
 **/
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class RedisListener extends SingleCodeListener {
    /**
     * 消费者监听stream key
     */
    private String streamKey;

    /**
     * 消费者所在消费者组
     */
    private String group;

    /**
     * 延时队列 zSet key
     */
    private String zSetKey;

    /**
     * 分布式锁 key
     */
    private String lockKey;

    public RedisListener(SingleCodeListener listener) {
        super(listener, listener.getCode());
    }

    public static List<RedisListener> getAllListeners(ListenerRegistry registry) {
        List<RedisListener> listeners = getTypeAllListeners(registry.getTimelyListeners());
        listeners.addAll(getTypeAllListeners(registry.getDelayListeners()));
        return listeners;
    }

    public static List<RedisListener> getTypeAllListeners(List<Listener> listeners) {
        return getMsgTypeAllListeners(listeners, EXPRESSION_FUNCTION, RedisListener.class);
    }

    private static final FieldExpressionFunction EXPRESSION_FUNCTION = (MsgType busType, boolean retry) -> {
        List<FieldExpression> expressions = new ArrayList<>(4);
        // 及时消息
        if (busType.isTimely()) {
            if (retry) {
                expressions.add(create(RedisListener::getStreamKey, RedisConstant.TIMELY_RETRY_QUEUE, Param.FULL_TOPIC));
                expressions.add(create(RedisListener::getZSetKey, RedisConstant.TIMELY_RETRY_ZSET, Param.FULL_TOPIC));
                expressions.add(create(RedisListener::getLockKey, RedisConstant.TIMELY_RETRY_LOCK, Param.FULL_TOPIC));
            } else {
                expressions.add(create(RedisListener::getStreamKey, RedisConstant.TIMELY_QUEUE, Param.TOPIC));
            }
        } else {
            if (retry) {
                expressions.add(create(RedisListener::getStreamKey, RedisConstant.DELAY_RETRY_QUEUE, Param.FULL_TOPIC));
                expressions.add(create(RedisListener::getZSetKey, RedisConstant.DELAY_RETRY_ZSET, Param.FULL_TOPIC));
                expressions.add(create(RedisListener::getLockKey, RedisConstant.DELAY_RETRY_LOCK, Param.FULL_TOPIC));
            } else {
                expressions.add(create(RedisListener::getStreamKey, RedisConstant.DELAY_QUEUE, Param.TOPIC));
                expressions.add(create(RedisListener::getZSetKey, RedisConstant.DELAY_ZSET, Param.TOPIC));
                expressions.add(create(RedisListener::getLockKey, RedisConstant.DELAY_LOCK, Param.TOPIC));
            }
        }
        // 固定消费者组
        expressions.add(createGroup());
        return expressions;
    };

    /**
     * 创建消费者组
     */
    private static FieldExpression createGroup() {
        return create(RedisListener::getGroup, "%s", Param.DELIVER_ID);
    }
}
