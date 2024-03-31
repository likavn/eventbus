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
import com.github.likavn.eventbus.core.api.MsgSender;
import com.github.likavn.eventbus.core.base.DefaultMsgDelayListener;
import com.github.likavn.eventbus.core.exception.EventBusException;
import com.github.likavn.eventbus.core.metadata.BusConfig;
import com.github.likavn.eventbus.core.metadata.InterceptorConfig;
import com.github.likavn.eventbus.core.metadata.MsgType;
import com.github.likavn.eventbus.core.metadata.data.Request;
import com.github.likavn.eventbus.core.metadata.support.FailTrigger;
import com.github.likavn.eventbus.core.metadata.support.Subscriber;
import com.github.likavn.eventbus.core.metadata.support.Trigger;
import com.github.likavn.eventbus.core.utils.Func;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;

/**
 * 消息投递分发器
 *
 * @author likavn
 * @date 2024/01/01
 **/
@Slf4j
public class DeliveryBus {
    private final InterceptorConfig interceptorConfig;
    private final BusConfig config;
    private final MsgSender msgSender;
    private final SubscriberRegistry registry;

    public DeliveryBus(InterceptorConfig interceptorConfig,
                       BusConfig config,
                       MsgSender msgSender,
                       SubscriberRegistry registry) {
        this.interceptorConfig = interceptorConfig;
        this.config = config;
        this.msgSender = msgSender;
        this.registry = registry;
    }

    /**
     * 发送及时消息给订阅者
     *
     * @param subscriber 订阅者
     * @param body       内容的主体
     */
    public void deliverTimely(Subscriber subscriber, byte[] body) {
        deliverTimely(subscriber, Func.convertByBytes(body));
    }

    /**
     * 发送及时消息给订阅者
     *
     * @param subscriber 订阅者
     * @param body       内容的主体
     */
    public void deliverTimely(Subscriber subscriber, String body) {
        deliverTimely(subscriber, Func.convertByJson(body));
    }

    /**
     * 发送及时消息给订阅者
     *
     * @param subscriber 订阅者
     * @param request    请求对象
     */
    public void deliverTimely(Subscriber subscriber, Request<?> request) {
        if (null != request.getDeliverId()) {
            // 判断当前订阅者是否等于消息投递ID
            if (!subscriber.getTrigger().getDeliverId().equals(request.getDeliverId())) {
                return;
            }
        }
        // 发送消息给订阅者
        deliver(subscriber, request);
    }

    /**
     * 接收延时消息
     *
     * @param body body
     */
    public void deliverDelay(String body) {
        deliverDelay(Func.convertByJson(body));
    }

    /**
     * 接收延时消息
     *
     * @param body body
     */
    public void deliverDelay(byte[] body) {
        deliverDelay(Func.convertByBytes(body));
    }

    /**
     * 接收延时消息
     *
     * @param body body
     */
    @SuppressWarnings("all")
    public void deliverDelay(Request request) {
        // 获取延时订阅者
        Subscriber subscriber = registry.getSubscriberDelay(request);
        if (null != subscriber) {
            // 如果触发器的调用对象是DefaultMsgDelayListener
            if (subscriber.getTrigger()
                    .getInvokeBean() instanceof DefaultMsgDelayListener) {
                // 如果消息类型是延时消息，则获取对应的延时订阅者
                if (MsgType.DELAY == request.getType()) {
                    subscriber = registry.getSubscriberDelay(request.getCode());
                } else {
                    // 否则获取对应的订阅者
                    subscriber = registry.getSubscriber(request.getDeliverId());
                }
            }
        }

        // 如果订阅者为空，则打印错误日志并返回
        if (null == subscriber) {
            log.error("delay msg handler not found class={}", request.getDelayListener().getName());
            return;
        }
        // 交付消息给订阅者
        deliver(subscriber, request);
    }

    /**
     * 投递消息
     */
    private void deliver(Subscriber subscriber, Request<?> request) {
        Trigger trigger = subscriber.getTrigger();
        if (null == request.getDeliverId()) {
            request.setDeliverId(trigger.getDeliverId());
        }
        if (log.isDebugEnabled()) {
            log.debug("deliver msg：{}", Func.toJson(request));
        }
        try {
            trigger.invoke(request);
            interceptorConfig.deliverSuccessExecute(request);
        } catch (Exception exception) {
            failHandle(subscriber, request, exception);
        }
    }

    /**
     * 失败处理
     *
     * @param subscriber subscriber
     * @param request    request
     * @param throwable  throwable
     */
    private void failHandle(Subscriber subscriber, Request<?> request, Throwable throwable) {
        if (!(throwable.getCause() instanceof InvocationTargetException)) {
            throw new EventBusException(throwable.getCause());
        }
        // 获取异常的真正原因
        throwable = throwable.getCause().getCause();
        // 发生异常时记录错误日志
        log.error("deliver error", throwable);
        // 获取订阅器的FailTrigger
        FailTrigger failTrigger = subscriber.getFailTrigger();
        Fail fail = null;
        if (null != failTrigger) {
            // 如果FailTrigger不为空，则获取Fail对象
            fail = failTrigger.getFail();
        }

        // 获取有效的投递次数
        int deliverNum = (null != fail && fail.retry() >= 0) ? fail.retry() : config.getFail().getRetryNum();
        if (request.getDeliverNum() <= deliverNum) {
            // 如果请求的投递次数小于等于有效的投递次数，则重新尝试投递
            failReTry(request, fail);
            return;
        }
        try {
            // 如果FailTrigger不为空，则执行订阅器的异常处理
            if (null != failTrigger && null != failTrigger.getMethod()) {
                failTrigger.invoke(request, throwable);
            }

            // 如果全局拦截器配置不为空且包含投递异常拦截器，则执行全局拦截器的异常处理
            interceptorConfig.deliverThrowableExecute(request, throwable);
        } catch (Exception var2) {
            // 捕获异常并记录错误日志
            log.error("deliveryBus.failHandle error", var2);
        }
    }

    /**
     * 失败重试
     *
     * @param request req
     * @param fail    fail
     */
    private void failReTry(Request<?> request, Fail fail) {
        // 获取下次投递失败时间
        long delayTime = (null != fail && fail.nextTime() > 0) ? fail.nextTime() : config.getFail().getNextTime();
        request.setDelayTime(delayTime);
        // 如果没有设置延时监听器，则设置默认的延时监听器
        if (null == request.getDelayListener()) {
            request.setDelayListener(DefaultMsgDelayListener.class);
        }

        // 投递次数加一
        request.setDeliverNum(request.getDeliverNum() + 1);
        msgSender.sendDelayMessage(request);
    }
}
