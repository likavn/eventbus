package com.github.likavn.eventbus.core;

import com.github.likavn.eventbus.core.annotation.Fail;
import com.github.likavn.eventbus.core.annotation.Subscribe;
import com.github.likavn.eventbus.core.annotation.SubscribeDelay;
import com.github.likavn.eventbus.core.api.MsgDelayListener;
import com.github.likavn.eventbus.core.api.MsgSubscribeListener;
import com.github.likavn.eventbus.core.base.DefaultMsgDelayListener;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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
    private final Map<String, List<Subscriber>> subscriberMap = new ConcurrentHashMap<>();
    /**
     * 订阅延时消息处理器
     * key=code
     * 注解:
     *
     * @see SubscribeDelay
     */
    private final Map<String, Subscriber> subscriberDelayMap = new ConcurrentHashMap<>();

    /**
     * 延时消息处理器
     */
    @SuppressWarnings("all")
    private final Map<Class<? extends MsgDelayListener>, Subscriber> msgDelayListenerMap = new ConcurrentHashMap<>();

    /**
     * 注册默认延时消息处理器
     */
    private final DefaultMsgDelayListener defaultDelayMsgListener = new DefaultMsgDelayListener(this);

    private final BusConfig config;

    public SubscriberRegistry(BusConfig config) {
        this.config = config;
    }

    /**
     * 注册器
     */
    public void register(Collection<Object> objs) {
        Assert.notEmpty(objs, "初始化实例失败！");
        objs.forEach(this::register);

        // 注册默认延时消息处理器
        register(defaultDelayMsgListener);
    }

    /**
     * 注册器
     */
    @SuppressWarnings("all")
    public void register(Object obj) {
        // class
        Class<?> cla = obj.getClass();
        Fail fail;
        // 接口实现的消息订阅器
        if (obj instanceof MsgSubscribeListener || obj instanceof MsgDelayListener) {
            Trigger trigger = getTrigger(obj, BusConstant.ON_MESSAGE);
            fail = trigger.getMethod().getAnnotation(Fail.class);
            if (null == fail) {
                fail = cla.getAnnotation(Fail.class);
            }
            FailTrigger failTrigger = null == fail ? null : new FailTrigger(fail, getTrigger(obj, fail.callMethod()));
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
                MsgDelayListener<?> listener = (MsgDelayListener<?>) obj;
                Subscriber subscriber = new Subscriber();
                subscriber.setServiceId(config.getServiceId());
                subscriber.setType(MsgType.DELAY);
                subscriber.setTrigger(trigger);
                subscriber.setFailTrigger(failTrigger);
                msgDelayListenerMap.put(listener.getClass(), subscriber);
            }
            return;
        }

        // 注解实现的消息订阅器
        for (Method method : cla.getMethods()) {
            // 存在及时消息订阅
            Subscribe subscribe = method.getAnnotation(Subscribe.class);
            SubscribeDelay subscribeDelay = method.getAnnotation(SubscribeDelay.class);
            if (null == subscribe && null == subscribeDelay) {
                continue;
            }
            Trigger trigger = Trigger.of(obj, method);

            // 订阅器类型
            MsgType msgType = null != subscribe ? MsgType.TIMELY : MsgType.DELAY;

            String serviceId = msgType.isTimely() ? subscribe.serviceId() : null;
            serviceId = Func.isEmpty(serviceId) ? config.getServiceId() : serviceId;
            // 获取投递异常处理
            fail = msgType.isTimely() ? subscribe.fail() : subscribeDelay.fail();
            FailTrigger failTrigger = Func.isEmpty(fail.callMethod()) ? null : new FailTrigger(fail, getTrigger(obj, fail.callMethod()));
            for (String code : msgType.isTimely() ? subscribe.codes() : subscribeDelay.codes()) {
                Subscriber subscriber = new Subscriber(serviceId, code, msgType, trigger, failTrigger);
                if (msgType.isTimely()) {
                    putSubscriberMap(subscriber);
                } else {
                    putSubscriberDelayMap(subscriber);
                }
            }
        }
    }

    /**
     * 新增订阅器
     *
     * @param subscriber subscriber
     */
    private void putSubscriberMap(Subscriber subscriber) {
        String deliverId = subscriber.getTrigger().getDeliverId();

        List<Subscriber> subscribers = subscriberMap.get(deliverId);
        if (Func.isEmpty(subscribers)) {
            subscribers = new ArrayList<>(1);
        }
        subscribers.add(subscriber);
        subscriberMap.put(deliverId, subscribers);
    }

    private void putSubscriberDelayMap(Subscriber subscriber) {
        Assert.isTrue(!subscriberDelayMap.containsKey(subscriber.getCode()),
                "subscribeDelay code=" + subscriber.getCode() + "存在相同的延时消息处理器");
        subscriberDelayMap.put(subscriber.getCode(), subscriber);
    }

    /**
     * 获取触发器
     *
     * @param obj        _this
     * @param methodName method name
     * @return tg
     */
    private Trigger getTrigger(Object obj, String methodName) {
        Class<?> cla = obj.getClass();
        Method method = null;
        for (Method mt : cla.getMethods()) {
            if (mt.getName().equals(methodName)) {
                method = mt;
                break;
            }
        }
        Assert.notNull(method, cla.getName() + " Miss method " + methodName);
        return Trigger.of(obj, method);
    }

    public Subscriber getSubscriber(String deliverId) {
        List<Subscriber> subscribers = subscriberMap.get(deliverId);
        if (Func.isEmpty(subscribers)) {
            return null;
        }
        return subscribers.get(0);
    }

    public Subscriber getSubscriberDelay(String code) {
        return subscriberDelayMap.get(code);
    }

    public Subscriber getMsgDelayListener(Class<? extends MsgDelayListener> cla) {
        return msgDelayListenerMap.get(cla);
    }

    public List<Subscriber> getSubscribers() {
        return subscriberMap.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
    }
}
