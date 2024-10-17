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
package com.github.likavn.eventbus.provider.rabbit.support;

import com.github.likavn.eventbus.core.ListenerRegistry;
import com.github.likavn.eventbus.core.metadata.MsgType;
import com.github.likavn.eventbus.core.metadata.support.Listener;
import com.github.likavn.eventbus.provider.SingleCodeListener;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

import static com.github.likavn.eventbus.provider.SingleCodeListener.FieldExpression.*;
import static com.github.likavn.eventbus.provider.rabbit.constant.RabbitConstant.*;

/**
 * rabbitmq消息订阅监听器消费者实体数据
 *
 * @author likavn
 * @date 2023/1/7
 **/
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class RabbitListener extends SingleCodeListener {
    /**
     * 交换机
     */
    private String exchange;

    /**
     * 消费者所在消费者组
     */
    private String routingKey;

    /**
     * 队列
     */
    private String queue;

    public RabbitListener(SingleCodeListener listener) {
        super(listener, listener.getCode());
    }

    public static List<RabbitListener> getAllListeners(ListenerRegistry registry) {
        List<RabbitListener> listeners = getTypeAllListeners(registry.getTimelyListeners());
        listeners.addAll(getTypeAllListeners(registry.getDelayListeners()));
        return listeners;
    }

    public static List<RabbitListener> getTypeAllListeners(List<Listener> listeners) {
        return getMsgTypeAllListeners(listeners, EXPRESSION_FUNCTION, RabbitListener.class);
    }

    /**
     * 获取消息类型所有监听器表达式
     */
    private static final FieldExpressionFunction EXPRESSION_FUNCTION = (MsgType busType, boolean retry) -> {
        List<FieldExpression> expressions = new ArrayList<>(3);
        // 设置交换机
        expressions.add(create(RabbitListener::getExchange, (busType.isTimely() && !retry) ? TIMELY_EXCHANGE : DELAY_EXCHANGE, Param.SERVICE_ID));
        if (busType.isTimely()) {
            if (retry) {
                expressions.add(create(RabbitListener::getQueue, TIMELY_RETRY_QUEUE, Param.FULL_TOPIC));
                expressions.add(create(RabbitListener::getRoutingKey, TIMELY_RETRY_ROUTING_KEY, Param.FULL_TOPIC));
            } else {
                expressions.add(create(RabbitListener::getQueue, TIMELY_QUEUE, Param.FULL_TOPIC));
                expressions.add(create(RabbitListener::getRoutingKey, TIMELY_ROUTING_KEY, Param.TOPIC));
            }
        } else {
            if (retry) {
                expressions.add(create(RabbitListener::getQueue, DELAY_RETRY_QUEUE, Param.FULL_TOPIC));
                expressions.add(create(RabbitListener::getRoutingKey, DELAY_RETRY_ROUTING_KEY, Param.FULL_TOPIC));
            } else {
                expressions.add(create(RabbitListener::getQueue, DELAY_QUEUE, Param.FULL_TOPIC));
                expressions.add(create(RabbitListener::getRoutingKey, DELAY_ROUTING_KEY, Param.TOPIC));
            }
        }
        return expressions;
    };
}
