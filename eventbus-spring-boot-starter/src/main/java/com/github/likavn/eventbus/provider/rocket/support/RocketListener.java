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
package com.github.likavn.eventbus.provider.rocket.support;

import com.github.likavn.eventbus.core.ListenerRegistry;
import com.github.likavn.eventbus.core.metadata.MsgType;
import com.github.likavn.eventbus.core.metadata.support.Listener;
import com.github.likavn.eventbus.provider.SingleCodeListener;
import com.github.likavn.eventbus.provider.rocket.constant.RocketConstant;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

import static com.github.likavn.eventbus.provider.SingleCodeListener.FieldExpression.Param;
import static com.github.likavn.eventbus.provider.SingleCodeListener.FieldExpression.create;

/**
 * rocketmq消息订阅监听器消费者实体数据
 *
 * @author likavn
 * @date 2023/1/7
 **/
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class RocketListener extends SingleCodeListener {
    /**
     * 消费者监听stream key
     */
    private String queue;

    /**
     * 消费者所在消费者组
     */
    private String group;

    public RocketListener(SingleCodeListener listener) {
        super(listener, listener.getCode());
    }

    public static List<RocketListener> getAllListeners(ListenerRegistry registry) {
        List<RocketListener> listeners = getTypeAllListeners(registry.getTimelyListeners());
        listeners.addAll(getTypeAllListeners(registry.getDelayListeners()));
        listeners.forEach(t -> {
            t.setQueue(keyFormat(t.getQueue()));
            t.setGroup(keyFormat(t.getGroup()));
        });
        return listeners;
    }

    public static List<RocketListener> getTypeAllListeners(List<Listener> listeners) {
        return getMsgTypeAllListeners(listeners, EXPRESSION_FUNCTION, RocketListener.class);
    }

    private static final FieldExpressionFunction EXPRESSION_FUNCTION = (MsgType busType, boolean retry) -> {
        List<FieldExpression> expressions = new ArrayList<>(2);
        // 及时消息
        if (busType.isTimely()) {
            if (retry) {
                expressions.add(create(RocketListener::getQueue, RocketConstant.TIMELY_RETRY_QUEUE, Param.FULL_TOPIC));
                expressions.add(createGroup(RocketConstant.TIMELY_RETRY_QUEUE));
            } else {
                expressions.add(create(RocketListener::getQueue, RocketConstant.TIMELY_QUEUE, Param.TOPIC));
                expressions.add(createGroup(RocketConstant.TIMELY_QUEUE));
            }
        } else {
            if (retry) {
                expressions.add(create(RocketListener::getQueue, RocketConstant.DELAY_RETRY_QUEUE, Param.FULL_TOPIC));
                expressions.add(createGroup(RocketConstant.DELAY_RETRY_QUEUE));
            } else {
                expressions.add(create(RocketListener::getQueue, RocketConstant.DELAY_QUEUE, Param.TOPIC));
                expressions.add(createGroup(RocketConstant.DELAY_QUEUE));
            }
        }
        return expressions;
    };

    public static FieldExpression createGroup(String groupFormat) {
        return create(RocketListener::getGroup, groupFormat, Param.FULL_TOPIC);
    }

    public static String keyFormat(String key) {
        return key.replace(".", "_");
    }
}
