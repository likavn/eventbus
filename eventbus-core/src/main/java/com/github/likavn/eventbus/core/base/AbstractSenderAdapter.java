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

import java.util.*;

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
    private final boolean sendTimelyToDelay;
    private final Map<String, List<Listener>> timelyToDelayListenerMap = new HashMap<>(4);

    protected AbstractSenderAdapter(BusConfig config,
                                    InterceptorConfig interceptorConfig,
                                    RequestIdGenerator requestIdGenerator,
                                    ListenerRegistry registry) {
        this.config = config;
        this.interceptorConfig = interceptorConfig;
        this.requestIdGenerator = requestIdGenerator;
        this.sendTimelyToDelay = config.getSendTimelyToDelay();
        initListener(registry);
    }

    /**
     * 初始化监听器注册表中的监听器
     * 该方法主要做两件事：
     * 1. 构建及时消息监听器的映射表{@code timelyToDelayListenerMap}，键为消息码，值为监听器列表
     * 2. 构建延迟消息监听器的映射表{@code listenerCodeDelayMap}，键为消息码，值为延迟监听器实例
     *
     * @param registry 监听器注册表，包含及时监听器和延迟监听器
     */
    private void initListener(ListenerRegistry registry) {
        // 遍历注册表中的及时监听器，过滤并构建消息码与监听器的映射
        registry.getTimelyListeners()
                .stream()
                // 如果监听器设置了延迟投递，且不需要第一次投递，则跳过
                .filter(listener -> null != listener.getToDelay() && !listener.getToDelay().firstDeliver())
                // // 如果监听器的消息码列表为空，则跳过
                .filter(listener -> !Func.isEmpty(listener.getTopics()))
                .forEach(listener -> {
                    // 遍历监听器的消息码，构建消息码与监听器的映射
                    listener.getTopics().forEach(topic -> {
                        List<Listener> listeners = timelyToDelayListenerMap.get(topic);
                        if (null == listeners) {
                            listeners = new ArrayList<>(1);
                        }
                        listeners.add(listener);
                        timelyToDelayListenerMap.put(topic, listeners);
                    });
                });
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
            // 发送延迟消息
            sendDelayMessage(request, false);
        });
        // 恢复原始的投递ID和延迟时间
        request.setDeliverId(deliverId);
        request.setDelayTime(delayTime);
    }

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
    protected void sendDelayMessage(Request<?> request, boolean interceptor) {
        request.setType(null == request.getType() ? MsgType.DELAY : request.getType());
        checkBuild(request);
        // 确保延迟时间被正确设置且大于0
        Assert.isTrue(null != request.getDelayTime() && request.getDelayTime() > 0, "延时时间不能小于0");
        // 如果启用了拦截器，则在发送前执行拦截器逻辑
        if (interceptor) {
            interceptorConfig.sendBeforeExecute(request);
        }
        toSendDelayMessage(request);
        // 如果启用了拦截器，则在发送后执行拦截器逻辑
        if (interceptor) {
            interceptorConfig.sendAfterExecute(request);
        }
    }

    /**
     * 发送消息
     *
     * @param request req
     */
    public abstract void toSend(Request<?> request);

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
