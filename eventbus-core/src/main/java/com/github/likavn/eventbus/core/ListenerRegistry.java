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
package com.github.likavn.eventbus.core;

import com.github.likavn.eventbus.core.annotation.EventbusListener;
import com.github.likavn.eventbus.core.annotation.FailRetry;
import com.github.likavn.eventbus.core.annotation.Polling;
import com.github.likavn.eventbus.core.annotation.ToDelay;
import com.github.likavn.eventbus.core.api.MsgDelayListener;
import com.github.likavn.eventbus.core.api.MsgListener;
import com.github.likavn.eventbus.core.constant.BusConstant;
import com.github.likavn.eventbus.core.exception.EventBusException;
import com.github.likavn.eventbus.core.metadata.BusConfig;
import com.github.likavn.eventbus.core.metadata.MsgType;
import com.github.likavn.eventbus.core.metadata.support.FailTrigger;
import com.github.likavn.eventbus.core.metadata.support.Listener;
import com.github.likavn.eventbus.core.metadata.support.Trigger;
import com.github.likavn.eventbus.core.utils.Assert;
import com.github.likavn.eventbus.core.utils.Func;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;


/**
 * 订阅器注册中心
 *
 * @author likavn
 * @date 2024/01/01
 **/
@Slf4j
public class ListenerRegistry {
    /**
     * 订阅及时消息处理器
     * key=订阅器全类名+方法名{@link Trigger#getDeliverId()}
     * 接口：
     *
     * @see MsgListener
     */
    private final Map<String, Listener> timelyMap = new ConcurrentHashMap<>();

    /**
     * 订阅延时消息处理器
     * key=订阅器全类名+方法名{@link Trigger#getDeliverId()}
     * 接口：
     *
     * @see MsgDelayListener
     */
    private final Map<String, Listener> delayMap = new ConcurrentHashMap<>();

    private final BusConfig config;

    /**
     * 构造器
     *
     * @param config 配置
     */
    public ListenerRegistry(BusConfig config) {
        this.config = config;
    }

    /**
     * 注册器
     */
    public void register(Collection<Object> objs) {
        if (Func.isEmpty(objs)) {
            log.warn("listener init fail, Because there is no @EventbusListener listener");
            return;
        }
        objs.forEach(this::register);
    }

    /**
     * 注册器
     */
    public void register(Object obj) {
        Class<?> listenerClass = Func.originalClass(obj);
        EventbusListener eventbusListener = listenerClass.getAnnotation(EventbusListener.class);
        if (null == eventbusListener) {
            return;
        }
        // 检查是否为多监听接口实现
        validMultiListener(listenerClass);
        // 接口实现的消息订阅器
        if (obj instanceof MsgListener) {
            register(obj, listenerClass, eventbusListener);
        }
    }

    /**
     * 注册事件监听器
     * <p>
     * 本函数核心目的是将事件监听器（Listener）注册到相应的数据结构中，以便在事件触发时能够快速响应
     * 主要包括以下步骤：
     * 1. 确保目标类具有指定的事件处理方法（on_message）
     * 2. 确定服务ID，优先使用事件监听器中标注的serviceId，如果未指定则使用配置中的serviceId
     * 3. 获取监听的事件代码列表
     * 4. 确定并发模式，根据事件监听器中标注的并发模式获取对应的并发数
     * 5. 获取事件触发器，用于描述事件如何触发
     * 6. 处理失败策略，如果方法上标注了失败策略（Fail），则创建对应的失败触发器（FailTrigger）
     * 7. 处理轮询策略，如果方法上标注了轮询策略（Polling），则创建对应的轮询触发器（Polling）
     * 8. 创建监听器实例，并根据对象类型决定其处理消息的类型（及时消息或延迟消息）
     * 9. 最终将监听器实例注册到相应的数据结构中（根据消息类型不同，存储在不同的Map中）
     *
     * @param obj              注册的对象实例，通常是一个事件监听器
     * @param listenerClass    目标类的Class对象，用于反射操作
     * @param eventbusListener 事件监听器标注，包含服务ID、监听代码、并发模式等信息
     */
    private void register(Object obj, Class<?> listenerClass, EventbusListener eventbusListener) {
        // 确保目标类具有指定的事件处理方法
        Method originalMethod = getMsgListenerMethod(listenerClass, BusConstant.ON_MESSAGE);
        if (null == originalMethod) {
            return;
        }
        // 确定服务ID
        String serviceId = Func.isEmpty(eventbusListener.serviceId()) ? config.getServiceId() : eventbusListener.serviceId();
        // 获取监听的事件代码列表
        List<String> codes = Func.getListenerCodes(listenerClass, eventbusListener);
        // 确定并发模式
        Integer concurrency = getConcurrency(eventbusListener.concurrency(), config.getConcurrency());
        // 重发/重试消息接收并发数
        Integer retryConcurrency = getConcurrency(eventbusListener.retryConcurrency(), config.getRetryConcurrency());
        // 获取事件触发器
        Trigger trigger = getTrigger(obj, BusConstant.ON_MESSAGE);
        // 处理失败策略
        FailRetry failRetry = originalMethod.getAnnotation(FailRetry.class);
        FailTrigger failTrigger = FailTrigger.of(failRetry, getTrigger(obj, BusConstant.FAIL_HANDLER));
        // 处理轮询策略
        Polling polling = originalMethod.getAnnotation(Polling.class);
        // 创建监听器实例
        Listener listener = new Listener(serviceId, codes, concurrency, retryConcurrency, trigger, failTrigger, polling);
        // 根据对象类型决定其处理消息的类型
        if (obj instanceof MsgDelayListener) {
            listener.setType(MsgType.DELAY);
            putDelayMap(listener);
        } else {
            ToDelay toDelay = originalMethod.getAnnotation(ToDelay.class);
            listener.setType(MsgType.TIMELY);
            listener.setToDelay(toDelay);
            putTimelyMap(listener);
        }
    }

    /**
     * 检查是否为多监听接口实现
     */
    private void validMultiListener(Class<?> listenerClass) {
        boolean isClassListener = false;
        Class<?> superclass = listenerClass.getSuperclass();
        if (Func.isInterfaceImpl(superclass, MsgListener.class)) {
            isClassListener = true;
        }
        int infListenerCount = 0;
        for (Class<?> inf : listenerClass.getInterfaces()) {
            if (Func.isInterfaceImpl(inf, MsgListener.class)) {
                infListenerCount++;
            }
        }
        if ((isClassListener && infListenerCount > 0) || infListenerCount > 1) {
            throw new EventBusException(listenerClass.getName() + " Not inherit together MsgListener and MsgDelayListener, or inherit multi MsgListener");
        }
    }

    /**
     * 获取并发数
     */
    private Integer getConcurrency(int concurrency, Integer defaultConcurrency) {
        return concurrency > 0 ? concurrency : defaultConcurrency;
    }

    /**
     * 新增订阅器
     *
     * @param listener listener
     */
    private void putTimelyMap(Listener listener) {
        listener.isValid();
        String deliverId = listener.getDeliverId();
        Assert.isTrue(!timelyMap.containsKey(deliverId), "timelyMap deliverId=" + deliverId + "存在相同的消息处理器");
        log.debug("ListenerRegistry 注册及时消息监听器deliverId={}", deliverId);
        timelyMap.put(deliverId, listener);
    }

    /**
     * 新增订阅器
     *
     * @param listener listener
     */
    private void putDelayMap(Listener listener) {
        listener.isValid();
        String deliverId = listener.getDeliverId();
        Assert.isTrue(!delayMap.containsKey(deliverId), "delayMap deliverId=" + deliverId + "存在相同的消息处理器");
        log.debug("ListenerRegistry 注册延时消息监听器deliverId={}", deliverId);
        delayMap.put(deliverId, listener);
    }

    /**
     * 获取触发器
     *
     * @param obj        _this
     * @param methodName method name
     * @return tg
     */
    private Trigger getTrigger(Object obj, String methodName) {
        return Trigger.of(obj, getMsgListenerMethod(obj.getClass(), methodName));
    }

    /**
     * 获取触发器
     *
     * @param cla        cla
     * @param methodName method name
     * @return mt
     */
    private Method getMsgListenerMethod(Class<?> cla, String methodName) {
        if (Func.isEmpty(methodName)) {
            return null;
        }
        Method supMethod = Stream.of(MsgListener.class.getDeclaredMethods()).filter(t -> t.getName().equals(methodName)).findFirst().orElse(null);
        if (null == supMethod) {
            throw new IllegalArgumentException("methodName=" + methodName + " not found in MsgListener");
        }
        Method method;
        try {
            method = cla.getMethod(methodName, supMethod.getParameterTypes());
            // 如果方法声明在MsgListener接口中，则表示为默认方法，无法获取，需要设置为null
            if (method.getDeclaringClass() == MsgListener.class) {
                method = null;
            }
        } catch (NoSuchMethodException e) {
            log.error("ListenerRegistry getMsgListenerMethod error", e);
            throw new EventBusException(e);
        }
        return method;
    }

    /**
     * 获取所有及时消息订阅器
     *
     * @return listeners
     */
    public List<Listener> getTimelyListeners() {
        return new ArrayList<>(timelyMap.values());
    }

    /**
     * 获取所有延时消息订阅器
     *
     * @return listeners
     */
    public List<Listener> getDelayListeners() {
        return new ArrayList<>(delayMap.values());
    }
}
