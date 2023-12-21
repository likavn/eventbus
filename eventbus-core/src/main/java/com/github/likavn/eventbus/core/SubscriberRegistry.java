package com.github.likavn.eventbus.core;

import com.github.likavn.eventbus.core.annotation.Fail;
import com.github.likavn.eventbus.core.annotation.Subscribe;
import com.github.likavn.eventbus.core.annotation.SubscribeDelay;
import com.github.likavn.eventbus.core.api.MsgDelayListener;
import com.github.likavn.eventbus.core.api.MsgSubscribeListener;
import com.github.likavn.eventbus.core.base.DefaultMsgDelayListener;
import com.github.likavn.eventbus.core.constant.MsgConstant;
import com.github.likavn.eventbus.core.metadata.BusConfig;
import com.github.likavn.eventbus.core.metadata.support.FailTrigger;
import com.github.likavn.eventbus.core.metadata.support.Subscriber;
import com.github.likavn.eventbus.core.metadata.support.Trigger;
import com.github.likavn.eventbus.core.utils.Assert;
import com.github.likavn.eventbus.core.utils.Func;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 订阅器注册中心
 *
 * @author likavn
 * @date 2023/6/29
 **/
@Slf4j
@UtilityClass
public class SubscriberRegistry {
    /**
     * 订阅消息处理器
     * key=serviceId+code,使用方法获取 {@link Func#getTopic(String, String)}
     * 注解:
     *
     * @see Subscribe
     * @see SubscribeDelay
     * 接口：
     * @see MsgSubscribeListener
     */
    private final Map<String, Subscriber> subscriberMap = new ConcurrentHashMap<>();

    /**
     * 延时消息处理器
     */
    @SuppressWarnings("all")
    private final Map<Class<? extends MsgDelayListener>, Subscriber> delayMsgListenerMap = new ConcurrentHashMap<>();

    /**
     * 注册器
     */
    public void register(BusConfig config, Collection<Object> objs) {
        Assert.notEmpty(objs, "初始化实例失败！");
        objs.forEach(obj -> register(config, obj));

        // 注册默认延时消息处理器
        DefaultMsgDelayListener defaultDelayMsgListener = new DefaultMsgDelayListener();
        register(config, defaultDelayMsgListener);
    }

    /**
     * 注册器
     */
    public void register(BusConfig config, Object obj) {
        // class
        Class<?> cla = obj.getClass();
        Fail fail;
        // 接口实现的消息订阅器
        if (obj instanceof MsgSubscribeListener || obj instanceof MsgDelayListener) {
            Trigger trigger = getTrigger(obj, MsgConstant.ON_MESSAGE);
            fail = cla.getAnnotation(Fail.class);
            if (null == fail) {
                fail = trigger.getMethod().getAnnotation(Fail.class);
            }
            FailTrigger failTrigger = null == fail ? null : new FailTrigger(fail, getTrigger(obj, fail.callback()));
            // 接口实现的及时消息订阅器
            if (obj instanceof MsgSubscribeListener) {
                MsgSubscribeListener<?> listener = (MsgSubscribeListener<?>) obj;
                String serviceId = Func.isEmpty(listener.getServiceId()) ? config.getServiceId() : listener.getServiceId();
                listener.getCodes().forEach(code -> {
                    Subscriber subscriber = new Subscriber(serviceId, code, false, trigger, failTrigger);
                    putSubscriberMap(subscriber);
                });
            }
            // 接口实现的延时消息处理器
            else {
                MsgDelayListener<?> listener = (MsgDelayListener<?>) obj;
                Subscriber subscriber = new Subscriber();
                subscriber.setServiceId(config.getServiceId());
                subscriber.setDelayMsg(true);
                subscriber.setTrigger(trigger);
                subscriber.setFailTrigger(failTrigger);
                delayMsgListenerMap.put(listener.getClass(), subscriber);
            }
            return;
        }
        // 方法
        for (Method method : cla.getMethods()) {
            // 存在及时消息订阅
            Subscribe subscribe = method.getAnnotation(Subscribe.class);
            SubscribeDelay subscribeDelay = method.getAnnotation(SubscribeDelay.class);
            if (null != subscribe || null != subscribeDelay) {
                Trigger trigger = new Trigger(obj, method, method.getParameterTypes());

                // 是否是延时消息
                boolean delayMsg = null != subscribeDelay;

                String serviceId = delayMsg ? config.getServiceId() : subscribe.serviceId();
                serviceId = Func.isEmpty(serviceId) ? config.getServiceId() : serviceId;
                // 获取投递异常处理
                fail = delayMsg ? subscribeDelay.fail() : subscribe.fail();
                FailTrigger failTrigger = Func.isEmpty(fail.callback()) ? null : new FailTrigger(fail, getTrigger(obj, fail.callback()));
                String[] codes = delayMsg ? subscribeDelay.code() : subscribe.code();
                for (String code : codes) {
                    Subscriber subscriber = new Subscriber(serviceId, code, delayMsg, trigger, failTrigger);
                    putSubscriberMap(subscriber);
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
        String topic = Func.getTopic(subscriber.getServiceId(), subscriber.getCode());
        Assert.isTrue(!subscriberMap.containsKey(topic), "serviceId or code Repeat " + topic);
        subscriberMap.put(topic, subscriber);
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
        return new Trigger(obj, method, method.getParameterTypes());
    }

    public Subscriber getSubscriber(String code) {
        return subscriberMap.get(code);
    }

    public Subscriber getDelayMsgListener(Class<? extends MsgDelayListener> cla) {
        return delayMsgListenerMap.get(cla);
    }
}
