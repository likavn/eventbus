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

import com.github.likavn.eventbus.core.ListenerRegistry;
import com.github.likavn.eventbus.core.api.RequestIdGenerator;
import com.github.likavn.eventbus.core.base.AbstractSenderAdapter;
import com.github.likavn.eventbus.core.base.InterceptorContainer;
import com.github.likavn.eventbus.core.metadata.BusConfig;
import com.github.likavn.eventbus.core.metadata.data.Request;
import com.github.likavn.eventbus.provider.rocket.constant.RocketConstant;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;

import static com.github.likavn.eventbus.provider.rocket.support.RocketListener.keyFormat;

/**
 * rocketMq生产者
 *
 * @author likavn
 * @since 2023/01/01
 * @since 2.2
 */
public class RocketMsgSender extends AbstractSenderAdapter {
    private final RocketMQTemplate rocketMqTemplate;

    public RocketMsgSender(RocketMQTemplate rocketMqTemplate,
                           BusConfig config,
                           InterceptorContainer interceptorContainer,
                           RequestIdGenerator requestIdGenerator,
                           ListenerRegistry registry) {
        super(config, interceptorContainer, requestIdGenerator, registry);
        this.rocketMqTemplate = rocketMqTemplate;
    }

    @Override
    public void toSend(Request<?> request) {
        rocketMqTemplate.syncSend(keyFormat(String.format(RocketConstant.TIMELY_QUEUE, request.topic()))
                + ":" + request.getServiceId(), request.toJson());
    }

    @Override
    public void toSendDelayMessage(Request<?> request) {
        String destination = getDestination(request);
        // 构建消息对象
        Message<String> message = MessageBuilder
                .withPayload(request.toJson())
                // 消息类型
                .setHeader(MessageHeaders.CONTENT_TYPE, "text/plain")
                .build();
        rocketMqTemplate.syncSend(
                destination + ":" + request.getServiceId(),
                message,
                // 发送超时时间,十秒
                10000,
                Math.toIntExact(request.getDelayTime()));
    }

    private String getDestination(Request<?> request) {
        return keyFormat(getDelayKey(request, RocketConstant.DELAY_QUEUE, RocketConstant.DELAY_RETRY_QUEUE, RocketConstant.TIMELY_RETRY_QUEUE));
    }
}
