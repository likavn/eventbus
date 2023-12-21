package com.github.likavn.eventbus.core.base;

import com.github.likavn.eventbus.core.api.MsgSender;
import com.github.likavn.eventbus.core.api.interceptor.SendAfterInterceptor;
import com.github.likavn.eventbus.core.api.interceptor.SendBeforeInterceptor;
import com.github.likavn.eventbus.core.metadata.BusConfig;
import com.github.likavn.eventbus.core.metadata.data.Request;
import com.github.likavn.eventbus.core.utils.Assert;

import java.util.UUID;

/**
 * 发送消息体包装处理类
 *
 * @author likavn
 * @since 2023/01/01
 */
@SuppressWarnings("all")
public abstract class AbstractSenderAdapter implements MsgSender {
    private SendBeforeInterceptor beforeInterceptor;
    private SendAfterInterceptor afterInterceptor;
    private BusConfig config;

    public AbstractSenderAdapter(SendBeforeInterceptor beforeInterceptor,
                                 SendAfterInterceptor afterInterceptor,
                                 BusConfig config) {
        this.beforeInterceptor = beforeInterceptor;
        this.afterInterceptor = afterInterceptor;
        this.config = config;
    }

    @Override
    public void send(Request<?> request) {
        request.setIsOrgSub(Boolean.TRUE);
        wrap(request);
        beforeInterceptor(request);
        toSend(request);
        afterInterceptor(request);
    }

    public abstract void toSend(Request<?> request);

    @Override
    public void sendDelayMessage(Request<?> request) {
        wrap(request);
        beforeInterceptor(request);
        toSendDelayMessage(request);
        afterInterceptor(request);
    }

    public abstract void toSendDelayMessage(Request<?> request);

    /**
     * 发送前拦截器
     *
     * @param request
     */
    private void beforeInterceptor(Request<?> request) {
        if (null != beforeInterceptor) {
            beforeInterceptor.execute(request);
        }
    }

    /**
     * 发送后拦截器
     *
     * @param request
     */
    private void afterInterceptor(Request<?> request) {
        if (null != afterInterceptor) {
            afterInterceptor.execute(request);
        }
    }

    /**
     * 发送消息前置操作
     *
     * @param request request
     * @return t
     */
    protected void wrap(Request<?> request) {
        Assert.notNull(request.getBody(), "消息体不能为空");
        if (null == request.getServiceId()) {
            request.setServiceId(config.getServiceId());
        }
        if (null == request.getRequestId()) {
            request.setRequestId(UUID.randomUUID().toString().replaceAll("-", ""));
        }
        if (null == request.getDeliverNum()) {
            request.setDeliverNum(1);
        }
        if (null != request.getDelayMsgHandler()) {
            Assert.isTrue(!(null == request.getDelayTime() || 0 >= request.getDelayTime()), "delayTime is null or zreo");
        }
    }
}
