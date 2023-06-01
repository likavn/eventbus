package com.github.likavn.notify.base;

import com.github.likavn.notify.api.MsgSender;
import com.github.likavn.notify.domain.Request;
import com.github.likavn.notify.utils.SpringUtil;
import org.springframework.util.Assert;

import java.util.UUID;

/**
 * 发送消息体包装处理类
 *
 * @author likavn
 * @since 2023/01/01
 */
@SuppressWarnings("all")
public abstract class AbstractMsgWrapSenderHandler implements MsgSender {
    @Override
    public void send(Request<?> request) {
        request.setIsOrgSub(Boolean.TRUE);
        wrap(request);
        toSend(request);
    }

    public abstract void toSend(Request<?> request);

    @Override
    public void sendDelayMessage(Request<?> request) {
        wrap(request);
        toSendDelayMessage(request);
    }

    public abstract void toSendDelayMessage(Request<?> request);

    /**
     * 发送消息前置操作
     *
     * @param request request
     * @return t
     */
    protected void wrap(Request<?> request) {
        Assert.notNull(request.getBody(), "消息体不能为空");
        if (null == request.getServiceId()) {
            request.setServiceId(SpringUtil.getServiceId());
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
