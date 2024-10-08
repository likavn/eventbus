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
import com.github.likavn.eventbus.core.metadata.data.MsgBody;
import com.github.likavn.eventbus.core.metadata.support.FailTrigger;
import com.github.likavn.eventbus.core.metadata.support.Listener;
import com.github.likavn.eventbus.core.metadata.support.Trigger;
import com.github.likavn.eventbus.core.utils.Assert;
import com.github.likavn.eventbus.core.utils.Func;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
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
        Assert.notEmpty(objs, "初始化实例失败！");
        objs.forEach(this::register);
    }

    /**
     * 注册器
     */
    public void register(Object obj) {
        Class<?> originalClass = Func.originalClass(obj);
        EventbusListener eventbusListener = originalClass.getAnnotation(EventbusListener.class);
        if (null == eventbusListener) {
            return;
        }
        // 接口实现的消息订阅器
        if (obj instanceof MsgListener) {
            register(obj, originalClass, eventbusListener);
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
     * @param originalClass    目标类的Class对象，用于反射操作
     * @param eventbusListener 事件监听器标注，包含服务ID、监听代码、并发模式等信息
     */
    private void register(Object obj, Class<?> originalClass, EventbusListener eventbusListener) {
        // 确保目标类具有指定的事件处理方法
        Method originalMethod = getMsgListenerMethod(originalClass, BusConstant.ON_MESSAGE);
        if (null == originalMethod) {
            return;
        }
        // 确定服务ID
        String serviceId = Func.isEmpty(eventbusListener.serviceId()) ? config.getServiceId() : eventbusListener.serviceId();
        // 获取监听的事件代码列表
        List<String> codes = getListenerCodes(originalClass, eventbusListener);
        // 确定并发模式
        Integer concurrency = getConcurrency(eventbusListener.concurrency());
        // 获取事件触发器
        Trigger trigger = getTrigger(obj, BusConstant.ON_MESSAGE);
        // 处理失败策略
        FailRetry failRetry = originalMethod.getAnnotation(FailRetry.class);
        FailTrigger failTrigger = FailTrigger.of(failRetry, getTrigger(obj, BusConstant.FAIL_HANDLER));
        // 处理轮询策略
        Polling polling = originalMethod.getAnnotation(Polling.class);
        // 创建监听器实例
        Listener listener = new Listener(serviceId, codes, concurrency, trigger, failTrigger, polling);
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
     * 根据类和注解获取事件监听器的代码
     *
     * @param originalClass    带有事件监听器注解的类
     * @param eventbusListener 事件监听器注解
     * @return 监听器代码列表
     * <p>
     * 该方法首先尝试通过注解中的codes属性获取代码如果未设置，则尝试通过继承关系
     * 和MsgBody接口获取代码如果这两种方式都失败，则抛出异常
     */
    private List<String> getListenerCodes(Class<?> originalClass, EventbusListener eventbusListener) {
        // 检查注解中的codes属性是否已设置
        String[] codes = eventbusListener.codes();
        if (!Func.isEmpty(codes)) {
            return Arrays.asList(codes);
        }
        Class<?> msgBodyClass = getMsgBodyClass(originalClass);
        // 检查消息体类是否实现了MsgBody接口
        if (Func.isInterfaceImpl(msgBodyClass, MsgBody.class)) {
            try {
                // 尝试获取消息体类的默认构造函数
                Constructor<?> constructor = msgBodyClass.getConstructor();
                // 如果构造函数不是可访问的，则设置为可访问
                if (!constructor.isAccessible()) {
                    constructor.setAccessible(true);
                }
                // 通过反射创建消息体类的实例，并获取其code方法的返回值
                String code = ((MsgBody) msgBodyClass.newInstance()).code();
                return Collections.singletonList(code);
            } catch (Exception e) {
                // 如果在反射过程中发生异常，则抛出EventBusException
                throw new EventBusException(e);
            }
        }
        return Collections.singletonList(originalClass.getSimpleName());
    }

    /**
     * 获取消息体的类类型
     *
     * @param originalClass 原始类类型，预计从中找出实现消息监听器接口的泛型参数
     * @return 返回实现消息监听器接口的泛型参数类类型
     * @throws EventBusException 如果没有找到实现消息监听器接口的类类型，则抛出此异常
     */
    private Class<?> getMsgBodyClass(Class<?> originalClass) {
        // 初始化超级接口类型变量
        Type superInf = null;

        // 遍历原始类的所有泛型接口
        for (Type inf : originalClass.getGenericInterfaces()) {
            // 如果接口不是参数化类型，则跳过
            if (!(inf instanceof ParameterizedType)) {
                continue;
            }

            // 获取接口的原始类类型
            Class<?> clz = (Class<?>) ((ParameterizedType) inf).getRawType();

            // 判断当前接口是否为MsgListener或MsgDelayListener接口的实现
            if (clz.getName().equals(MsgListener.class.getName())
                    || Func.isInterfaceImpl(clz, MsgListener.class)
                    || clz.getName().equals(MsgDelayListener.class.getName())
                    || Func.isInterfaceImpl(clz, MsgDelayListener.class)) {
                superInf = inf;
            }
        }

        // 如果没有找到实现的消息监听器接口，则抛出异常
        if (null == superInf) {
            throw new EventBusException("The message listener implementation interface was not found");
        }

        // 返回消息监听器接口的泛型参数类型
        return (Class<?>) ((ParameterizedType) superInf).getActualTypeArguments()[0];
    }

    /**
     * 获取并发数
     *
     * @param defaultConcurrency 默认并发数
     * @return 并发数
     */
    private Integer getConcurrency(Integer defaultConcurrency) {
        return null == defaultConcurrency || defaultConcurrency < 1 ? config.getConcurrency() : defaultConcurrency;
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
        Method method = null;
        try {
            method = cla.getMethod(methodName, supMethod.getParameterTypes());
            // 如果方法声明在MsgListener接口中，则表示为默认方法，无法获取，需要设置为null
            if (method.getDeclaringClass() == MsgListener.class) {
                method = null;
            }
        } catch (NoSuchMethodException ignored) {
        }
        return method;
    }

    /**
     * 获取订阅器
     *
     * @param deliverId deliverId
     * @return listener
     */
    public Listener getTimelyListener(String deliverId) {
        return timelyMap.get(deliverId);
    }

    /**
     * 获取延时消息处理器
     *
     * @param deliverId deliverId
     * @return listener
     */
    public Listener getDelayListener(String deliverId) {
        return delayMap.get(deliverId);
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

    /**
     * 获取所有消息订阅器
     *
     * @return listeners
     */
    public List<Listener> getFullListeners() {
        List<Listener> listeners = new ArrayList<>(getTimelyListeners());
        listeners.addAll(getDelayListeners());
        return listeners;
    }
}
