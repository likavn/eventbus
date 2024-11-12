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

import com.github.likavn.eventbus.core.annotation.FailRetry;
import com.github.likavn.eventbus.core.annotation.Polling;
import com.github.likavn.eventbus.core.annotation.ToDelay;
import com.github.likavn.eventbus.core.api.MsgSender;
import com.github.likavn.eventbus.core.exception.EventBusException;
import com.github.likavn.eventbus.core.metadata.BusConfig;
import com.github.likavn.eventbus.core.base.InterceptorContainer;
import com.github.likavn.eventbus.core.metadata.data.Request;
import com.github.likavn.eventbus.core.metadata.support.FailTrigger;
import com.github.likavn.eventbus.core.metadata.support.Listener;
import com.github.likavn.eventbus.core.metadata.support.Trigger;
import com.github.likavn.eventbus.core.utils.CalculateUtil;
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
    private final InterceptorContainer interceptorContainer;
    private final BusConfig config;
    private final MsgSender msgSender;

    public DeliveryBus(InterceptorContainer interceptorContainer,
                       BusConfig config,
                       MsgSender msgSender) {
        this.interceptorContainer = interceptorContainer;
        this.config = config;
        this.msgSender = msgSender;
    }

    /**
     * 发送及时消息给订阅者
     *
     * @param listener 订阅者
     * @param body     内容的主体
     */
    public void deliver(Listener listener, byte[] body) {
        deliver(listener, Func.convertByBytes(body));
    }

    /**
     * 发送及时消息给订阅者
     *
     * @param listener 订阅者
     * @param body     内容的主体
     */
    public void deliver(Listener listener, String body) {
        deliver(listener, Func.convertByJson(body));
    }

    /**
     * 执行消息投递到指定的监听器
     * 主要功能包括触发监听器的相应操作，并处理投递过程中可能出现的异常
     *
     * @param listener 监听器对象，用于接收消息并执行相应操作
     * @param request  请求对象，包含投递所需的信息
     */
    private void deliver(Listener listener, Request<?> request) {
        // 获取监听器的触发条件
        Trigger trigger = listener.getTrigger();
        // 如果请求中没有指定投递ID，则使用触发条件中的投递ID
        if (null == request.getDeliverId()) {
            request.setDeliverId(trigger.getDeliverId());
        }
        // 如果开启了调试日志，则记录消息内容
        if (log.isDebugEnabled()) {
            log.debug("deliver msg：{}", request.toJson());
        }
        try {
            // 触发监听器，根据触发条件执行相应操作
            trigger(listener, trigger, request);
        } catch (Exception exception) {
            // 处理投递失败的情况
            failHandle(listener, request, exception);
        }
    }

    /**
     * 触发事件给订阅者
     * 本方法负责根据订阅者的状态和请求的属性来决定是否立即触发事件，延迟触发，或者不触发
     *
     * @param listener 事件的订阅者，实现了Listener接口
     * @param trigger  事件触发器
     * @param request  请求对象，包含了触发事件所需的信息
     * @throws InvocationTargetException 如果触发事件的方法抛出异常
     * @throws IllegalAccessException    如果无法访问触发事件的方法
     */
    private void trigger(Listener listener, Trigger trigger, Request<?> request) throws InvocationTargetException, IllegalAccessException {
        // 标记是否最终投递事件
        boolean isDeliver = false;
        // 获取订阅者的延迟投递策略
        ToDelay toDelay = listener.getToDelay();
        // 如果订阅者没有设置延迟投递策略，则直接投递事件
        if (null == toDelay || request.isRetry()) {
            isDeliver = invoke(trigger, request);
        } else {
            // 如果订阅者设置了一开始就投递，则投递事件
            if (toDelay.firstDeliver()) {
                isDeliver = invoke(trigger, request);
            } else if (request.isToDelay()) {
                // 如果请求被标记为可延迟，并且设置了延迟时间大于0，则投递事件
                if (request.getDelayTime() > 0) {
                    isDeliver = invoke(trigger, request);
                }
            }
        }
        // 如果事件被投递，则执行投递成功后的操作
        if (isDeliver) {
            interceptorContainer.deliverAfterExecute(request, null);
        }
        // 如果订阅者不应该被延迟投递，则进行轮询操作
        if (!toDelay(listener, request)) {
            polling(listener, request);
        }
    }

    /**
     * 调用触发器方法
     * <p>
     * 本方法用于统一调用触发器对象的invoke方法，并处理可能的异常
     * 它简化了触发器的调用过程，并提供了集中的异常处理
     *
     * @param trigger 触发器对象，其invoke方法将被调用
     * @param request 请求对象，作为触发器方法的参数
     * @return 总是返回true，表示调用总是尝试执行
     * @throws InvocationTargetException 如果触发器方法抛出异常，则此异常被抛出
     * @throws IllegalAccessException    如果触发器对象或其方法无法访问，此异常被抛出
     */
    private boolean invoke(Trigger trigger, Request<?> request) throws InvocationTargetException, IllegalAccessException {
        // 执行投递前的拦截器
        interceptorContainer.deliverBeforeExecute(request);
        trigger.invoke(request);
        return true;
    }

    /**
     * 失败处理
     *
     * @param listener  listener
     * @param request   request
     * @param throwable throwable
     */
    private void failHandle(Listener listener, Request<?> request, Throwable throwable) {
        if (!(throwable instanceof InvocationTargetException)) {
            throw new EventBusException(throwable);
        }
        // 获取异常的真正原因
        throwable = throwable.getCause();
        // 发生异常时记录错误日志
        log.error("deliver error", throwable);
        // 获取订阅器的FailTrigger
        FailTrigger failTrigger = listener.getFailTrigger();
        FailRetry failRetry = null;
        if (null != failTrigger) {
            // 如果FailTrigger不为空，则获取Fail对象
            failRetry = failTrigger.getFail();
        }
        try {
            // 每次投递消息异常时都会调用
            interceptorContainer.deliverAfterExecute(request, throwable);
            // 如果FailTrigger不为空，则执行订阅器的异常处理
            if (null != failTrigger && null != failTrigger.getMethod()) {
                failTrigger.invoke(request, throwable);
            }
        } catch (Exception var2) {
            log.error("deliveryBus.failHandle error", var2);
        }
        // 获取有效的投递次数
        int retryCount = (null != failRetry && failRetry.count() >= 0) ? failRetry.count() : config.getFail().getRetryCount();
        int failRetryCount = request.getFailRetryCount();
        if (failRetryCount < retryCount) {
            // 如果请求的投递次数小于等于有效的投递次数，则重新尝试投递
            request.setFailRetryCount(failRetryCount + 1);
            failReTry(request, failRetry);
            return;
        }
        try {
            // 如果全局拦截器配置不为空且包含投递异常拦截器，则执行全局拦截器的异常处理
            interceptorContainer.deliverThrowableLastExecute(request, throwable);
        } catch (Exception var2) {
            log.error("deliveryBus.failHandle error", var2);
        }
    }

    /**
     * 失败重试
     *
     * @param request req
     * @param fail    fail
     */
    private void failReTry(Request<?> request, FailRetry fail) {
        // 获取下次投递失败时间
        long delayTime = (null != fail && fail.nextTime() > 0) ? fail.nextTime() : config.getFail().getNextTime();
        sendDelayMessage(request, delayTime);
    }

    /**
     * 轮询投递
     *
     * @param listener listener
     * @param request  req
     */
    private void polling(Listener listener, Request<?> request) {
        Polling polling = listener.getPolling();
        if (null == polling) {
            return;
        }
        // 已轮询次数大于轮询次数，则不进行轮询投递
        // 是否已退出轮询
        boolean isOver = Polling.Keep.clear();
        if (isOver) {
            return;
        }
        int pollingCount = request.getPollingCount();
        if (pollingCount >= polling.count()) {
            return;
        }
        pollingCount++;
        long delayTime = request.getDelayTime();
        String interval = polling.interval();
        interval = interval.replace("$count", String.valueOf(pollingCount))
                .replace("$deliverCount", String.valueOf(request.getDeliverCount()))
                .replace("$intervalTime", String.valueOf(0 == delayTime ? 1 : delayTime));
        // 获取下次投递失败时间
        delayTime = CalculateUtil.fixEvalExpression(interval);

        request.setPollingCount(pollingCount);
        sendDelayMessage(request, delayTime);
    }

    /**
     * 及时消息转换为延时消息
     * <p>
     * 此方法用于判断和转换及时消息为延时消息，根据订阅者的配置和请求的状态，
     * 决定是否将当前的及时消息转换为延时消息发送出去
     *
     * @param listener 订阅者对象，包含消息的配置信息和目标主题
     * @param request  请求对象，包含当前消息的详细信息和状态
     * @return 返回是否成功转换为延时消息，true表示成功转换，false表示未转换或不符合转换条件
     */
    private boolean toDelay(Listener listener, Request<?> request) {
        // 如果订阅者本身是延时类型的，则不需要转换
        if (listener.getType().isDelay()) {
            return false;
        }
        // 获取订阅者配置的延时发送信息
        ToDelay toDelay = listener.getToDelay();
        // 如果没有配置延时信息，则无法转换为延时消息
        if (null == toDelay) {
            return false;
        }
        // 如果消息已经被尝试多次投递，则不再转换为延时消息
        if (request.getDeliverCount() > 1) {
            return false;
        }
        // 如果配置为首次投递即转为延时消息，且当前消息属于同一主题，则不需要继续发送延时消息
        if (request.isToDelay()) {
            return 0 == request.getDelayTime();
        }
        // 发送延时消息，并返回转换成功
        request.setToDelay(true);
        sendDelayMessage(request, toDelay.delayTime());
        return true;
    }

    /**
     * 发送延迟消息
     * <p>
     * 注意：此方法用于处理需要延迟投递的消息，通过更新消息的投递信息来实现延迟投递效果
     *
     * @param request   消息请求对象，包含消息内容及投递相关信息
     * @param delayTime 延迟时间，表示距离下一次投递的时间间隔
     */
    private void sendDelayMessage(Request<?> request, Long delayTime) {
        // 设置下一次投递的时间，即当前时间加上延迟时间
        request.setDelayTime(delayTime);
        // 投递次数加一，表示消息已经尝试投递过一次
        request.setDeliverCount(request.getDeliverCount() + 1);
        request.setRetry(true);
        msgSender.sendDelayMessage(request);
    }
}