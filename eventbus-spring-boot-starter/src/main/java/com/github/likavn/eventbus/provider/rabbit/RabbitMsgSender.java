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

import com.github.likavn.eventbus.core.ListenerRegistry;
import com.github.likavn.eventbus.core.api.RequestIdGenerator;
import com.github.likavn.eventbus.core.base.AbstractSenderAdapter;
import com.github.likavn.eventbus.core.metadata.BusConfig;
import com.github.likavn.eventbus.core.metadata.InterceptorConfig;
import com.github.likavn.eventbus.core.metadata.data.Request;
import com.github.likavn.eventbus.core.utils.Func;
import com.github.likavn.eventbus.provider.rabbit.constant.RabbitConstant;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * rabbitMq生产者
 *
 * @author likavn
 * @since 2023/01/01
 */
public class RabbitMsgSender extends AbstractSenderAdapter {
    private final RabbitTemplate rabbitTemplate;

    public RabbitMsgSender(RabbitTemplate rabbitTemplate, BusConfig config,
                           InterceptorConfig interceptorConfig, RequestIdGenerator requestIdGenerator, ListenerRegistry registry) {
        super(config, interceptorConfig, requestIdGenerator, registry);
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void toSend(Request<?> request) {
        rabbitTemplate.convertAndSend(
                String.format(RabbitConstant.EXCHANGE, request.getServiceId()),
                String.format(RabbitConstant.ROUTING_KEY, request.topic()),
                Func.toJson(request),
                message -> {
                    message.getMessageProperties().setContentEncoding("utf-8");
                    return message;
                },
                new CorrelationData(request.getRequestId()));
    }

    @Override
    public void toSendDelayMessage(Request<?> request) {
        rabbitTemplate.convertAndSend(
                String.format(RabbitConstant.DELAY_EXCHANGE, request.getServiceId()),
                String.format(RabbitConstant.DELAY_ROUTING_KEY, request.topic()),
                Func.toJson(request),
                message -> {
                    //配置消息的过期时间,单位：毫秒
                    message.getMessageProperties().setHeader("x-delay", 1000L * request.getDelayTime());
                    return message;
                },
                new CorrelationData(request.getRequestId())
        );
    }
}
