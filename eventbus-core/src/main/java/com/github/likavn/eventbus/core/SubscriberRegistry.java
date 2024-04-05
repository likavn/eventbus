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

import com.github.likavn.eventbus.core.annotation.Fail;
import com.github.likavn.eventbus.core.annotation.Subscribe;
import com.github.likavn.eventbus.core.annotation.SubscribeDelay;
import com.github.likavn.eventbus.core.api.MsgDelayListener;
import com.github.likavn.eventbus.core.api.MsgSubscribeListener;
import com.github.likavn.eventbus.core.constant.BusConstant;
import com.github.likavn.eventbus.core.metadata.BusConfig;
import com.github.likavn.eventbus.core.metadata.MsgType;
import com.github.likavn.eventbus.core.metadata.support.FailTrigger;
import com.github.likavn.eventbus.core.metadata.support.Subscriber;
import com.github.likavn.eventbus.core.metadata.support.Trigger;
import com.github.likavn.eventbus.core.utils.Assert;
import com.github.likavn.eventbus.core.utils.Func;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 订阅器注册中心
 *
 * @author likavn
 * @date 2024/01/01
 **/
@Slf4j
public class SubscriberRegistry {
    /**
     * 订阅及时消息处理器
     * key=订阅器全类名+方法名{@link Trigger#getDeliverId()}
     * 注解:
     *
     * @see Subscribe
     * 接口：
     * @see MsgSubscribeListener
     */
    private final Map<String, Subscriber> subscriberMap = new ConcurrentHashMap<>();
    /**
     * 订阅延时消息处理器
     * key=订阅器全类名+方法名{@link Trigger#getDeliverId()}
     * 注解:
     *
     * @see SubscribeDelay
     * 接口：
     * @see MsgDelayListener
     */
    private final Map<String, Subscriber> subscriberDelayMap = new ConcurrentHashMap<>();

    private final BusConfig config;

    /**
     * 构造器
     *
     * @param config 配置
     */
    public SubscriberRegistry(BusConfig config) {
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
        // 接口实现的消息订阅器
        if (obj instanceof MsgSubscribeListener || obj instanceof MsgDelayListener) {
            registerInterfaceListeners(obj);
            return;
        }

        // 注解实现的消息订阅器
        Method[] methods = obj.getClass().getDeclaredMethods();
        registerAnnotationListeners(obj, Arrays.asList(methods));
    }

    /**
     * 注册接口实现的消息订阅器
     *
     * @param obj 实例
     */
    @SuppressWarnings("all")
    private void registerInterfaceListeners(Object obj) {
        // 获取对象的原始类型（存在代理类的情况）
        Class<?> primitiveClass = Func.primitiveClass(obj);
        Method primitiveMethod = getMethod(primitiveClass, BusConstant.ON_MESSAGE);
        if (null == primitiveMethod) {
            return;
        }
        Fail fail = primitiveMethod.getAnnotation(Fail.class);
        if (null == fail) {
            fail = primitiveClass.getAnnotation(Fail.class);
        }
        FailTrigger failTrigger = null == fail ? null : new FailTrigger(fail, getTrigger(obj, fail.callMethod()));
        // 获取触发器
        Trigger trigger = getTrigger(obj, BusConstant.ON_MESSAGE);
        // 接口实现的及时消息订阅器
        if (obj instanceof MsgSubscribeListener) {
            MsgSubscribeListener<?> listener = (MsgSubscribeListener<?>) obj;
            String serviceId = Func.isEmpty(listener.getServiceId()) ? config.getServiceId() : listener.getServiceId();
            listener.getCodes().forEach(code -> {
                Subscriber subscriber = new Subscriber(serviceId, code, MsgType.TIMELY, trigger, failTrigger);
                putSubscriberMap(subscriber);
            });
        }
        // 接口实现的延时消息处理器
        else {
            Subscriber subscriber = new Subscriber();
            subscriber.setServiceId(config.getServiceId());
            subscriber.setType(MsgType.DELAY);
            subscriber.setTrigger(trigger);
            subscriber.setFailTrigger(failTrigger);

            // 添加到延迟触发器订阅者映射表中
            putSubscriberDelayMap(Func.getDeliverId(primitiveClass, BusConstant.ON_MESSAGE), subscriber);
        }
    }

    /**
     * 注册注解实现的消息订阅器
     *
     * @param obj     实例对象
     * @param methods 方法列表
     **/
    private void registerAnnotationListeners(Object obj, List<Method> methods) {
        Class<?> primitiveClass = Func.primitiveClass(obj);
        AtomicBoolean isCreated = new AtomicBoolean(false);
        // 遍历方法列表
        methods.stream().filter(method -> {
            Method primitiveMethod = getMethod(primitiveClass, method.getName());
            if (null == primitiveMethod) {
                return false;
            }
            // 获取注解
            Subscribe subscribe = primitiveMethod.getAnnotation(Subscribe.class);
            SubscribeDelay subscribeDelay = primitiveMethod.getAnnotation(SubscribeDelay.class);

            // 判断注解是否存在
            if (null == subscribe && null == subscribeDelay) {
                return false;
            }

            // 判断是否已经创建过订阅器
            Assert.isTrue(!isCreated.get(), String.format("存在重复的订阅器，一个订阅器类只能存在一个，class：%s", obj.getClass()));
            isCreated.set(true);
            return true;
        }).forEach(method -> {
            Method primitiveMethod = getMethod(primitiveClass, method.getName());
            if (null == primitiveMethod) {
                return;
            }
            // 创建触发器
            Trigger trigger = Trigger.of(obj, method);

            MsgType msgType = MsgType.DELAY;
            String serviceId = config.getServiceId();
            Fail fail;
            List<String> codes;
            // 获取注解
            Subscribe subscribe = primitiveMethod.getAnnotation(Subscribe.class);
            if (null != subscribe) {
                msgType = MsgType.TIMELY;

                // 如果注解中指定了serviceId，则使用注解中的值
                if (!Func.isEmpty(subscribe.serviceId())) {
                    serviceId = subscribe.serviceId();
                }

                fail = subscribe.fail();
                codes = Arrays.asList(subscribe.codes());
            } else {
                // 获取注解
                SubscribeDelay subscribeDelay = primitiveMethod.getAnnotation(SubscribeDelay.class);
                fail = subscribeDelay.fail();
                codes = Arrays.asList(subscribeDelay.codes());
            }
            FailTrigger failTrigger = new FailTrigger(fail, getTrigger(obj, fail.callMethod()));

            // 遍历代码列表
            for (String code : codes) {
                // 创建订阅者
                Subscriber subscriber = new Subscriber(serviceId, code, msgType, trigger, failTrigger);
                if (msgType.isTimely()) {
                    // 添加到定时触发器订阅者映射表中
                    putSubscriberMap(subscriber);
                } else {
                    // 添加到延迟触发器订阅者映射表中
                    putSubscriberDelayMap(trigger.getDeliverId(), subscriber);
                }
            }
        });
    }


    /**
     * 新增订阅器
     *
     * @param subscriber subscriber
     */
    private void putSubscriberMap(Subscriber subscriber) {
        String deliverId = subscriber.getTrigger().getDeliverId();
        Assert.isTrue(!subscriberMap.containsKey(deliverId), "subscriberMap deliverId=" + deliverId + "存在相同的消息处理器");
        subscriberMap.put(deliverId, subscriber);
    }

    /**
     * 新增订阅器
     *
     * @param deliverId  投递ID
     * @param subscriber subscriber
     */
    private void putSubscriberDelayMap(String deliverId, Subscriber subscriber) {
        Assert.isTrue(!subscriberDelayMap.containsKey(deliverId), "subscribeDelay deliverId=" + deliverId + "存在相同的延时消息处理器");
        subscriberDelayMap.put(deliverId, subscriber);
    }

    /**
     * 获取触发器
     *
     * @param obj        _this
     * @param methodName method name
     * @return tg
     */
    private Trigger getTrigger(Object obj, String methodName) {
        return Trigger.of(obj, getMethod(obj.getClass(), methodName));
    }

    /**
     * 获取触发器
     *
     * @param cla        cla
     * @param methodName method name
     * @return mt
     */
    private Method getMethod(Class<?> cla, String methodName) {
        if (Func.isEmpty(methodName)) {
            return null;
        }
        Method method = null;
        for (Method mt : cla.getMethods()) {
            if (mt.getName().equals(methodName)) {
                method = mt;
                break;
            }
        }
        return method;
    }

    /**
     * 获取订阅器
     *
     * @param deliverId deliverId
     * @return subscriber
     */
    public Subscriber getSubscriber(String deliverId) {
        return subscriberMap.get(deliverId);
    }

    /**
     * 获取延时消息处理器
     *
     * @param deliverId deliverId
     * @return subscriber
     */
    public Subscriber getSubscriberDelay(String deliverId) {
        return subscriberDelayMap.get(deliverId);
    }

    /**
     * 获取所有及时消息订阅器
     *
     * @return subscribers
     */
    public List<Subscriber> getSubscribers() {
        return new ArrayList<>(subscriberMap.values());
    }

    /**
     * 获取所有延时消息订阅器
     *
     * @return subscribers
     */
    public List<Subscriber> getSubscriberDelays() {
        return new ArrayList<>(subscriberDelayMap.values());
    }
}
