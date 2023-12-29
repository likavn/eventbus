package com.github.likavn.eventbus.core;

import com.github.likavn.eventbus.core.annotation.Fail;
import com.github.likavn.eventbus.core.api.MsgSender;
import com.github.likavn.eventbus.core.metadata.BusConfig;
import com.github.likavn.eventbus.core.metadata.InterceptorConfig;
import com.github.likavn.eventbus.core.metadata.data.Request;
import com.github.likavn.eventbus.core.metadata.support.FailTrigger;
import com.github.likavn.eventbus.core.metadata.support.Subscriber;
import com.github.likavn.eventbus.core.utils.Func;
import lombok.extern.slf4j.Slf4j;

/**
 * 消息投递分发器
 *
 * @author likavn
 * @date 2024/01/01
 **/
@Slf4j
public class DeliverBus {
    private final InterceptorConfig interceptorConfig;
    private final BusConfig config;
    private final MsgSender msgSender;
    private final SubscriberRegistry registry;

    public DeliverBus(InterceptorConfig interceptorConfig,
                      BusConfig config,
                      MsgSender msgSender,
                      SubscriberRegistry registry) {
        this.interceptorConfig = interceptorConfig;
        this.config = config;
        this.msgSender = msgSender;
        this.registry = registry;
    }

    /**
     * 接收
     *
     * @param body body
     */
    public void deliver(Subscriber subscriber, byte[] body) {
        deliver(subscriber, Func.convertByBytes(body));
    }

    /**
     * 接收延时消息
     *
     * @param body body
     */
    @SuppressWarnings("all")
    public void deliverDelay(byte[] body) {
        Request request = Func.convertByBytes(body);
        Subscriber subscriber = registry.getMsgDelayListener(request.getDelayListener());
        if (null == subscriber) {
            log.error("delay msg handler not found class={}", request.getDelayListener().getName());
            return;
        }
        deliver(subscriber, request);
    }

    /**
     * 投递消息
     */
    public void deliver(Subscriber subscriber, Request<?> request) {
        if (log.isDebugEnabled()) {
            log.debug("deliver msg：{}", Func.toJson(request));
        }
        try {
            subscriber.getTrigger().invoke(request);
            // 投递成功 拦截器
            interceptorConfig.deliverSuccessExecute(request);
        } catch (Exception ex) {
            failHandle(subscriber, request, ex);
        }
    }

    /**
     * 失败处理
     *
     * @param subscriber subscriber
     * @param request    request
     * @param ex         ex
     */
    private void failHandle(Subscriber subscriber, Request<?> request, Exception ex) {
        // 发生异常时记录错误日志
        log.error("deliver error", ex);
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
            if (null != failTrigger) {
                failTrigger.invoke(request, ex);
            }

            // 如果全局拦截器配置不为空且包含投递异常拦截器，则执行全局拦截器的异常处理
            interceptorConfig.deliverExceptionExecute(request, ex);
        } catch (Exception var2) {
            // 捕获异常并记录错误日志
            log.error("DeliverBus.failHandle error", var2);
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

        // 投递次数加一
        request.setDeliverNum(request.getDeliverNum() + 1);
        msgSender.sendDelayMessage(request);
    }
}
