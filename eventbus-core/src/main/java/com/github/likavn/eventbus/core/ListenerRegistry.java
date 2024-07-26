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

import com.github.likavn.eventbus.core.annotation.DelayListener;
import com.github.likavn.eventbus.core.annotation.Fail;
import com.github.likavn.eventbus.core.annotation.Polling;
import com.github.likavn.eventbus.core.api.MsgDelayListener;
import com.github.likavn.eventbus.core.api.MsgListener;
import com.github.likavn.eventbus.core.constant.BusConstant;
import com.github.likavn.eventbus.core.metadata.BusConfig;
import com.github.likavn.eventbus.core.metadata.MsgType;
import com.github.likavn.eventbus.core.metadata.support.FailTrigger;
import com.github.likavn.eventbus.core.metadata.support.Listener;
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
public class ListenerRegistry {
    /**
     * 订阅及时消息处理器
     * key=订阅器全类名+方法名{@link Trigger#getDeliverId()}
     * 注解:
     *
     * @see Listener
     * 接口：
     * @see MsgListener
     */
    private final Map<String, Listener> timelyMap = new ConcurrentHashMap<>();

    /**
     * 订阅延时消息处理器
     * key=订阅器全类名+方法名{@link Trigger#getDeliverId()}
     * 注解:
     *
     * @see DelayListener
     * 接口：
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
        // 接口实现的消息订阅器
        if (obj instanceof MsgListener || obj instanceof MsgDelayListener) {
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
        Polling polling = primitiveMethod.getAnnotation(Polling.class);
        if (null == fail) {
            fail = primitiveClass.getAnnotation(Fail.class);
        }
        FailTrigger failTrigger = null == fail ? null : new FailTrigger(fail, getTrigger(obj, fail.callMethod()));
        // 获取触发器
        Trigger trigger = getTrigger(obj, BusConstant.ON_MESSAGE);
        // 接口实现的及时消息订阅器
        if (obj instanceof MsgListener) {
            MsgListener<?> interf = (MsgListener<?>) obj;
            String serviceId = Func.isEmpty(interf.getServiceId()) ? config.getServiceId() : interf.getServiceId();
            Integer concurrency = getConcurrency(interf.getConcurrency());
            interf.getCodes().forEach(code -> {
                Listener listener = new Listener(serviceId, code, concurrency, MsgType.TIMELY, trigger, failTrigger, polling);
                putTimelyMap(listener);
            });
        }
        // 接口实现的延时消息处理器
        else {
            Listener listener = new Listener();
            listener.setServiceId(config.getServiceId());
            listener.setType(MsgType.DELAY);
            listener.setTrigger(trigger);
            listener.setFailTrigger(failTrigger);
            MsgDelayListener interf = (MsgDelayListener) obj;
            listener.setConcurrency(config.getDelayConcurrency());
            listener.setPolling(polling);
            // 添加到延迟触发器订阅者映射表中
            putDelayMap(Func.getDeliverId(primitiveClass, BusConstant.ON_MESSAGE), listener);
        }
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
     * 注册注解监听器。
     * 该方法会遍历提供的方法列表，查找并注册带有特定注解的方法作为事件监听器。
     *
     * @param obj     要注册监听器的对象实例。
     * @param methods 要检查的方法列表。
     */
    private void registerAnnotationListeners(Object obj, List<Method> methods) {
        // 获取对象对应的原始类型
        Class<?> primitiveClass = Func.primitiveClass(obj);
        AtomicBoolean isCreated = new AtomicBoolean(false);

        // 遍历方法列表，筛选出带有特定注解的方法
        methods.stream().filter(method -> {
            // 尝试获取原始类型的同名方法
            Method primitiveMethod = getMethod(primitiveClass, method.getName());
            if (null == primitiveMethod) {
                return false;
            }

            // 获取并检查注解是否存在
            com.github.likavn.eventbus.core.annotation.Listener listener
                    = primitiveMethod.getAnnotation(com.github.likavn.eventbus.core.annotation.Listener.class);
            DelayListener delayListener = primitiveMethod.getAnnotation(DelayListener.class);

            if (null == listener && null == delayListener) {
                return false;
            }

            // 确保一个订阅器类只被注册一次
            Assert.isTrue(!isCreated.get(), String.format("存在重复的订阅器，一个订阅器类只能存在一个，class：%s", obj.getClass()));
            isCreated.set(true);
            return true;
        }).forEach(method -> registerAnnotationListeners(obj, primitiveClass, method));
    }


    /**
     * 注册注解实现的消息订阅器。
     * 该方法用于根据注解配置，将指定的方法注册为消息订阅器，根据注解中的配置项（如消息类型、错误处理方式、并发控制等），
     * 创建并添加到相应的订阅者映射表中（延迟或及时）。
     *
     * @param obj            实例对象，即包含订阅方法的对象实例。
     * @param primitiveClass 被订阅方法所在的原始类。
     * @param method         订阅方法。
     **/
    private void registerAnnotationListeners(Object obj, Class<?> primitiveClass, Method method) {
        // 尝试获取与给定方法同名的原始方法
        Method primitiveMethod = getMethod(primitiveClass, method.getName());
        if (null == primitiveMethod) {
            return; // 如果找不到原始方法，则直接返回
        }
        // 创建触发器，用于在指定条件满足时触发订阅方法
        Trigger trigger = Trigger.of(obj, method);

        // 默认消息类型为DELAY（延迟）
        MsgType msgType = MsgType.DELAY;
        // 默认服务ID为配置中的服务ID
        String serviceId = config.getServiceId();
        // 默认失败处理方式和并发控制未设置
        Fail fail;
        List<String> codes;
        Integer concurrency;

        Polling polling = primitiveMethod.getAnnotation(Polling.class);
        // 检查订阅方法是否注解了Listener注解
        com.github.likavn.eventbus.core.annotation.Listener listener
                = primitiveMethod.getAnnotation(com.github.likavn.eventbus.core.annotation.Listener.class);
        if (null != listener) {
            // 如果注解存在，则设置消息类型为TIMELY（及时）
            msgType = MsgType.TIMELY;

            // 如果注解中指定了serviceId，则使用注解中的值
            if (!Func.isEmpty(listener.serviceId())) {
                serviceId = listener.serviceId();
            }

            // 从注解中获取失败处理方式、错误码列表和并发控制设置
            fail = listener.fail();
            codes = Arrays.asList(listener.codes());
            concurrency = listener.concurrency();
        } else {
            // 检查是否注解了DelayListener注解
            DelayListener delayListener = primitiveMethod.getAnnotation(DelayListener.class);
            // 从DelayListener注解中获取失败处理方式、错误码列表和并发控制设置
            fail = delayListener.fail();
            codes = Arrays.asList(delayListener.codes());
            // 若未指定，则使用默认的并发控制值
            concurrency = config.getDelayConcurrency();
        }

        // 创建失败触发器，用于处理订阅执行失败的情况
        FailTrigger failTrigger = new FailTrigger(fail, getTrigger(obj, fail.callMethod()));

        // 遍历错误码列表，为每个错误码创建并注册一个订阅者
        for (String code : codes) {
            // 设置并发控制值，若未指定则使用默认值
            concurrency = getConcurrency(concurrency);
            // 创建订阅者实例
            Listener createListener = new Listener(serviceId, code, concurrency, msgType, trigger, failTrigger, polling);
            if (msgType.isTimely()) {
                // 如果是及时消息，则添加到及时触发器订阅者映射表中
                putTimelyMap(createListener);
            } else {
                // 如果是延迟消息，则添加到延迟触发器订阅者映射表中
                putDelayMap(trigger.getDeliverId(), createListener);
            }
        }
    }


    /**
     * 新增订阅器
     *
     * @param listener listener
     */
    private void putTimelyMap(Listener listener) {
        String deliverId = listener.getTrigger().getDeliverId();
        Assert.isTrue(!timelyMap.containsKey(deliverId), "listenerMap deliverId=" + deliverId + "存在相同的消息处理器");
        log.debug("ListenerRegistry 注册消息监听器deliverId={}", deliverId);
        timelyMap.put(deliverId, listener);
    }

    /**
     * 新增订阅器
     *
     * @param deliverId 投递ID
     * @param listener  listener
     */
    private void putDelayMap(String deliverId, Listener listener) {
        Assert.isTrue(!delayMap.containsKey(deliverId), "subscribeDelay deliverId=" + deliverId + "存在相同的延时消息处理器");
        log.debug("ListenerRegistry 注册消息监听器deliverId={}", deliverId);
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
}
