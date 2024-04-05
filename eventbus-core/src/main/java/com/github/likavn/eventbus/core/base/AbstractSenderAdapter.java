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
package com.github.likavn.eventbus.core.base;

import com.github.likavn.eventbus.core.SubscriberRegistry;
import com.github.likavn.eventbus.core.api.MsgSender;
import com.github.likavn.eventbus.core.api.RequestIdGenerator;
import com.github.likavn.eventbus.core.metadata.BusConfig;
import com.github.likavn.eventbus.core.metadata.InterceptorConfig;
import com.github.likavn.eventbus.core.metadata.MsgType;
import com.github.likavn.eventbus.core.metadata.data.Request;
import com.github.likavn.eventbus.core.metadata.support.Subscriber;
import com.github.likavn.eventbus.core.utils.Assert;
import com.github.likavn.eventbus.core.utils.Func;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 发送消息体包装处理类
 *
 * @author likavn
 * @date 2024/01/01
 */
public abstract class AbstractSenderAdapter implements MsgSender {
    private final BusConfig config;
    private final InterceptorConfig interceptorConfig;
    private final RequestIdGenerator requestIdGenerator;
    /**
     * key=code
     */
    private final Map<String, Subscriber> subscriberDelayMap;

    protected AbstractSenderAdapter(BusConfig config,
                                    InterceptorConfig interceptorConfig, RequestIdGenerator requestIdGenerator, SubscriberRegistry registry) {
        this.config = config;
        this.interceptorConfig = interceptorConfig;
        this.requestIdGenerator = requestIdGenerator;
        this.subscriberDelayMap = registry.getSubscriberDelays().stream().filter(t -> !Func.isEmpty(t.getCode()))
                .collect(Collectors.toMap(Subscriber::getCode, Function.identity(), (x, y) -> x));
    }

    @Override
    public void send(Request<?> request) {
        request.setType(MsgType.TIMELY);
        checkBuild(request);
        Assert.isTrue(!Func.isEmpty(request.getCode()), "及时消息code不能为空");
        interceptorConfig.sendBeforeExecute(request);
        toSend(request);
        interceptorConfig.sendAfterExecute(request);
    }

    /**
     * 发送消息
     *
     * @param request req
     */
    public abstract void toSend(Request<?> request);

    @Override
    public void sendDelayMessage(Request<?> request) {
        request.setType(null == request.getType() ? MsgType.DELAY : request.getType());
        checkBuild(request);
        Assert.isTrue(null != request.getDelayTime() && request.getDelayTime() > 0, "延时时间不能小于0");
        // 投递ID
        if (Func.isEmpty(request.getDeliverId())) {
            Subscriber subscriber = subscriberDelayMap.get(request.getCode());
            Assert.notNull(subscriber, "延时消息code未找到对应订阅器！");
            request.setDeliverId(subscriber.getTrigger().getDeliverId());
        }
        interceptorConfig.sendBeforeExecute(request);
        toSendDelayMessage(request);
        interceptorConfig.sendAfterExecute(request);
    }

    /**
     * 发送延时消息
     *
     * @param request req
     */
    public abstract void toSendDelayMessage(Request<?> request);

    /**
     * 发送消息前置操作
     *
     * @param request req
     */
    protected void checkBuild(Request<?> request) {
        // 确保传入的对象不为空
        Objects.requireNonNull(request.getBody(), "消息体不能为空");

        // 设置服务ID为默认值，如果为空的话
        request.setServiceId(Func.isEmpty(request.getServiceId()) ? config.getServiceId() : request.getServiceId());

        // 设置请求ID为默认值，如果为空的话
        request.setRequestId(Func.isEmpty(request.getRequestId()) ? requestIdGenerator.nextId() : request.getRequestId());

        // 设置递送数量为默认值，如果为空的话
        request.setDeliverCount(request.getDeliverCount() != null ? request.getDeliverCount() : 1);
    }
}
