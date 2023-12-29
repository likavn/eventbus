package com.github.likavn.eventbus.core.base;

import com.github.likavn.eventbus.core.api.MsgSender;
import com.github.likavn.eventbus.core.metadata.BusConfig;
import com.github.likavn.eventbus.core.metadata.InterceptorConfig;
import com.github.likavn.eventbus.core.metadata.MsgType;
import com.github.likavn.eventbus.core.metadata.data.Request;
import com.github.likavn.eventbus.core.utils.Assert;

import java.util.UUID;

/**
 * 发送消息体包装处理类
 *
 * @author likavn
 * @date 2024/01/01
 */
public abstract class AbstractSenderAdapter implements MsgSender {
    private final InterceptorConfig interceptorConfig;
    private final BusConfig config;

    public AbstractSenderAdapter(InterceptorConfig interceptorConfig, BusConfig config) {
        this.interceptorConfig = interceptorConfig;
        this.config = config;
    }

    @Override
    public void send(Request<?> request) {
        build(request);
        interceptorConfig.sendBeforeExecute(request);
        toSend(request);
        interceptorConfig.sendAfterExecute(request);
    }

    /**
     * 发送消息
     *
     * @param request req
     */
    public abstract void toSend(Request<?> request);

    @Override
    public void sendDelayMessage(Request<?> request) {
        request.setType(MsgType.DELAY);
        build(request);
        interceptorConfig.sendBeforeExecute(request);
        toSendDelayMessage(request);
        interceptorConfig.sendAfterExecute(request);
    }

    /**
     * 发送延时消息
     *
     * @param request req
     */
    public abstract void toSendDelayMessage(Request<?> request);

    /**
     * 发送消息前置操作
     *
     * @param request req
     */
    protected void build(Request<?> request) {
        Assert.notNull(request.getBody(), "消息体不能为空");
        if (null == request.getServiceId()) {
            request.setServiceId(config.getServiceId());
        }
        if (null == request.getRequestId()) {
            request.setRequestId(UUID.randomUUID().toString().replace("-", ""));
        }
        if (null == request.getDeliverNum()) {
            request.setDeliverNum(1);
        }
        if (null != request.getDelayListener()) {
            Assert.isTrue(!(null == request.getDelayTime() || 0 >= request.getDelayTime()), "delayTime is null or zreo");
        }
    }
}
