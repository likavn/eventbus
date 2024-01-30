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
import com.github.likavn.eventbus.core.metadata.data.Request;
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
    private final DefaultMsgDelayListener defaultDelayMsgListener = new DefaultMsgDelayListener();

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

        // 注册默认延时消息处理器
        register(defaultDelayMsgListener);
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
        registerAnnotationListeners(obj);
    }

    /**
     * 注册接口实现的消息订阅器
     *
     * @param obj 实例
     */
    private void registerInterfaceListeners(Object obj) {
        Trigger trigger = getTrigger(obj, BusConstant.ON_MESSAGE);
        Fail fail = trigger.getMethod().getAnnotation(Fail.class);
        if (null == fail) {
            fail = obj.getClass().getAnnotation(Fail.class);
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
    }

    /**
     * 注册注解实现的消息订阅器
     *
     * @param obj 实例
     **/
    private void registerAnnotationListeners(Object obj) {
        boolean isCreated = false;
        // 遍历对象的类方法集合
        for (Method method : obj.getClass().getDeclaredMethods()) {
            // 获取方法上的Subscribe注解和SubscribeDelay注解
            Subscribe subscribe = method.getAnnotation(Subscribe.class);
            SubscribeDelay subscribeDelay = method.getAnnotation(SubscribeDelay.class);

            // 如果注解都为空，则继续下一个方法
            if (null == subscribe && null == subscribeDelay) {
                continue;
            }

            // 确保一个订阅器类只有一个订阅器
            Assert.isTrue(!isCreated, String.format("存在重复的订阅器，一个订阅器类只能存在一个，class：%s", obj.getClass()));
            isCreated = true;

            // 创建触发器对象
            Trigger trigger = Trigger.of(obj, method);

            // 订阅器类型
            MsgType msgType = MsgType.DELAY;
            String serviceId = config.getServiceId();
            Fail fail;
            String[] codes;
            // 判断是否有Subscribe注解
            if (null != subscribe) {
                msgType = MsgType.TIMELY;

                // 从注解中获取serviceId
                if (!Func.isEmpty(subscribe.serviceId())) {
                    serviceId = subscribe.serviceId();
                }

                fail = subscribe.fail();
                codes = subscribe.codes();
            } else {
                // 从注解SubscribeDelay中获取投递异常处理
                fail = subscribeDelay.fail();
                codes = subscribeDelay.codes();
            }

            // 判断是否需要失败触发器
            FailTrigger failTrigger = Func.isEmpty(fail.callMethod()) ? null : new FailTrigger(fail, getTrigger(obj, fail.callMethod()));

            // 遍历code数组，创建Subscriber对象并存入相应的Map中
            for (String code : codes) {
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

    /**
     * 新增订阅器
     *
     * @param subscriber subscriber
     */
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

    /**
     * 获取订阅器
     *
     * @param deliverId deliverId
     * @return subscriber
     */
    public Subscriber getSubscriber(String deliverId) {
        List<Subscriber> subscribers = subscriberMap.get(deliverId);
        if (Func.isEmpty(subscribers)) {
            return null;
        }
        return subscribers.get(0);
    }

    /**
     * 获取延时消息处理器
     *
     * @param code code
     * @return subscriber
     */
    public Subscriber getSubscriberDelay(String code) {
        return subscriberDelayMap.get(code);
    }

    /**
     * 获取延时消息处理器
     *
     * @param cla class
     * @return subscriber
     */
    @SuppressWarnings("all")
    public Subscriber getMsgDelayListener(Class<? extends MsgDelayListener> cla) {
        return msgDelayListenerMap.get(cla);
    }

    /**
     * 获取延时消息处理器
     *
     * @param request request
     * @return subscriber
     */
    @SuppressWarnings("all")
    public Subscriber getSubscriberDelay(Request request) {
        Subscriber subscriber = null;
        if (null != request.getDelayListener()) {
            subscriber = getMsgDelayListener(request.getDelayListener());
        }
        if (null == subscriber) {
            subscriber = getSubscriberDelay(request.getCode());
        }
        return subscriber;
    }

    /**
     * 获取所有订阅器
     *
     * @return subscribers
     */
    public List<Subscriber> getSubscribers() {
        return subscriberMap.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
    }
}
