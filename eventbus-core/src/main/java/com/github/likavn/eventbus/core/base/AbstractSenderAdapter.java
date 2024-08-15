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

import com.github.likavn.eventbus.core.ListenerRegistry;
import com.github.likavn.eventbus.core.api.MsgSender;
import com.github.likavn.eventbus.core.api.RequestIdGenerator;
import com.github.likavn.eventbus.core.metadata.BusConfig;
import com.github.likavn.eventbus.core.metadata.InterceptorConfig;
import com.github.likavn.eventbus.core.metadata.MsgType;
import com.github.likavn.eventbus.core.metadata.data.Request;
import com.github.likavn.eventbus.core.metadata.support.Listener;
import com.github.likavn.eventbus.core.utils.Assert;
import com.github.likavn.eventbus.core.utils.Func;

import java.util.List;
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
    private final Map<String, List<Listener>> timelyToDelayListenerMap;

    /**
     * key=code
     */
    private final Map<String, Listener> listenerCodeDelayMap;

    private final boolean sendTimelyToDelay;

    protected AbstractSenderAdapter(BusConfig config,
                                    InterceptorConfig interceptorConfig,
                                    RequestIdGenerator requestIdGenerator,
                                    ListenerRegistry registry) {
        this.config = config;
        this.interceptorConfig = interceptorConfig;
        this.requestIdGenerator = requestIdGenerator;
        this.timelyToDelayListenerMap = registry.getTimelyListeners().stream().filter(t
                -> null != t.getToDelay() && !t.getToDelay().firstDeliver()).collect(Collectors.groupingBy(Listener::getTopic));

        this.listenerCodeDelayMap = registry.getDelayListeners().stream().filter(t
                -> !Func.isEmpty(t.getCode())).collect(Collectors.toMap(Listener::getCode, Function.identity(), (x, y) -> x));

        this.sendTimelyToDelay = config.getSendTimelyToDelay();
    }

    /**
     * 发送及时消息
     * 本方法主要负责将及时消息进行预处理，并通过检查构建项，设置消息类型，
     * 检查消息code是否为空，执行发送前的拦截器操作，然后进行实际的消息发送
     * 如果存在对应的延迟监听器，则将及时消息转为延迟消息发送
     * 最后执行发送后的拦截器操作
     *
     * @param request 待发送的消息请求对象，类型为泛型
     */
    @Override
    public void send(Request<?> request) {
        // 设置消息类型为及时消息
        request.setType(MsgType.TIMELY);
        // 检查消息请求对象的构建项是否正确
        checkBuild(request);
        // 断言消息code不为空
        Assert.isTrue(!Func.isEmpty(request.getCode()), "及时消息code不能为空");
        // 执行发送前的拦截器操作
        interceptorConfig.sendBeforeExecute(request);
        toDelay(request);
        // 执行实际的消息发送操作
        toSend(request);

        // 执行发送后的拦截器操作
        interceptorConfig.sendAfterExecute(request);
    }

    /**
     * 根据条件决定是否将消息设置为延迟投递
     * 此方法主要用于控制消息的延迟投递行为，避免在某些条件下立即投递
     *
     * @param request 消息请求对象，包含消息投递和延迟的相关信息
     */
    private void toDelay(Request<?> request) {
        // 如果消息已经有了投递ID，说明已经被投递，无需进行延迟处理
        if (!Func.isEmpty(request.getDeliverId())) {
            return;
        }
        // 如果消息被标记为需要延迟，并且当前状态不允许立即投递，则直接返回
        if (Boolean.TRUE.equals(request.getToDelay()) && !sendTimelyToDelay) {
            return;
        }
        // 如果当前消息的主题没有对应的延迟投递监听器，说明不需要进行延迟投递
        if (!timelyToDelayListenerMap.containsKey(request.topic())) {
            return;
        }
        // 标记消息为需要延迟投递
        request.setToDelay(Boolean.TRUE);
        // 保存原始的投递ID和延迟时间，以便后续恢复
        String deliverId = request.getDeliverId();
        Long delayTime = request.getDelayTime();
        // 获取消息主题对应的延迟投递监听器，并为每个监听器设置新的投递ID和延迟时间
        timelyToDelayListenerMap.get(request.topic()).forEach(listener -> {
            // 设置延迟消息的投递ID
            request.setDeliverId(listener.getTrigger().getDeliverId());
            // 设置延迟时间
            request.setDelayTime(listener.getToDelay().delayTime());
            // 发送延迟消息，不重新尝试
            sendDelayMessage(request, false);
        });
        // 恢复原始的投递ID和延迟时间
        request.setDeliverId(deliverId);
        request.setDelayTime(delayTime);
    }

    /**
     * 发送消息
     *
     * @param request req
     */
    public abstract void toSend(Request<?> request);

    @Override
    public void sendDelayMessage(Request<?> request) {
        sendDelayMessage(request, true);
    }

    /**
     * 发送延迟消息
     * 该方法用于处理发送延迟消息的逻辑，包括检查请求的配置，应用拦截器逻辑（如果启用），并最终发送延迟消息
     *
     * @param request     请求对象，包含了要发送的消息的所有信息以及配置
     * @param interceptor 一个布尔值，指示是否启用拦截器逻辑
     */
    public void sendDelayMessage(Request<?> request, boolean interceptor) {
        // 设置消息类型，如果请求中没有指定类型，则默认为延迟消息类型
        request.setType(null == request.getType() ? MsgType.DELAY : request.getType());
        // 检查并设置请求的必要配置
        checkBuild(request);
        // 确保延迟时间被正确设置且大于0
        Assert.isTrue(null != request.getDelayTime() && request.getDelayTime() > 0, "延时时间不能小于0");
        // 投递ID
        if (Func.isEmpty(request.getDeliverId())) {
            Listener subscriber = listenerCodeDelayMap.get(request.getCode());
            Assert.notNull(subscriber, "延时消息code未找到对应订阅器！");
            request.setDeliverId(subscriber.getTrigger().getDeliverId());
        }
        // 如果启用了拦截器，则在发送前执行拦截器逻辑
        if (interceptor) {
            interceptorConfig.sendBeforeExecute(request);
        }

        // 调用方法发送延迟消息
        toSendDelayMessage(request);

        // 如果启用了拦截器，则在发送后执行拦截器逻辑
        if (interceptor) {
            interceptorConfig.sendAfterExecute(request);
        }
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
