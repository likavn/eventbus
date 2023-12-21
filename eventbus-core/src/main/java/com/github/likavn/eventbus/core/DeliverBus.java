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
 * @date 2023/12/17
 **/
@Slf4j
public class DeliverBus {
    private final InterceptorConfig interceptorConfig;

    private final BusConfig config;

    private final MsgSender msgSender;

    public DeliverBus(InterceptorConfig interceptorConfig, BusConfig config, MsgSender msgSender) {
        this.interceptorConfig = interceptorConfig;
        this.config = config;
        this.msgSender = msgSender;
    }

    /**
     * 接收
     *
     * @param body body
     */
    public void deliver(byte[] body) {
        deliver(Func.convertByBytes(body));
    }

    /**
     * 消息分发投递
     *
     * @param request req
     */
    public void deliver(Request<?> request) {
        if (log.isDebugEnabled()) {
            log.debug("deliver msg：{}", Func.toJson(request));
        }
        Subscriber subscriber = trigger(request);
        if (null == subscriber) {
            log.warn("deliver code={} msg Missing subscriber!", request.getCode());
            return;
        }
        try {
            subscriber.getTrigger().invoke(request);
            // 投递成功 拦截器
            if (null != interceptorConfig && null != interceptorConfig.getDeliverSuccessInterceptor()) {
                interceptorConfig.getDeliverSuccessInterceptor().execute(request);
            }
        } catch (Exception ex) {
            log.error("deliver error", ex);
            FailTrigger failTrigger = subscriber.getFailTrigger();
            Fail fail = null;
            if (null != failTrigger) {
                fail = failTrigger.getFail();
            }

            // 获取有效的投递次数
            int deliverNum = null != fail ? fail.retry() : config.getFail().getRetryNum();
            if (request.getDeliverNum() <= deliverNum) {
                failReTry(request, fail);
                return;
            }
            try {
                // 执行订阅器异常处理
                if (null != failTrigger) {
                    failTrigger.invoke(request, ex);
                }

                // 投递失败时全局拦截器
                if (null != interceptorConfig && null != interceptorConfig.getDeliverExceptionInterceptor()) {
                    interceptorConfig.getDeliverExceptionInterceptor().execute(request, ex);
                }
            } catch (Exception var2) {
                log.error("DeliverBus.onFail error", var2);
            }
        }
    }

    /**
     * 消息触发
     *
     * @param request req
     */
    public Subscriber trigger(Request<?> request) {
        Class<?> delayMsgHandler = request.getDelayMsgHandler();
        // 延时队列
        if (null != delayMsgHandler) {
            return SubscriberRegistry.getDelayMsgListener(request.getDelayMsgHandler());
        }
        return SubscriberRegistry.getSubscriber(request.getTopic());
    }

    public void failReTry(Request<?> request, Fail fail) {
        // 获取下次投递失败时间
        long delayTime = null != fail ? fail.nextTime() : config.getFail().getNextTime();
        request.setDelayTime(delayTime);

        // 投递次数加一
        request.setDeliverNum(request.getDeliverNum() + 1);
        msgSender.sendDelayMessage(request);
    }
}
