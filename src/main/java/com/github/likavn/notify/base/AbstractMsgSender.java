package com.github.likavn.notify.base;

import com.github.likavn.notify.api.MsgSender;
import com.github.likavn.notify.domain.Request;
import com.github.likavn.notify.utils.SpringUtil;
import org.springframework.util.Assert;

import java.util.Objects;
import java.util.UUID;

/**
 * 延时消息处理类
 *
 * @author likavn
 * @since 2023/01/01
 */
@SuppressWarnings("all")
public abstract class AbstractMsgSender implements MsgSender {

    /**
     * 发送消息前置操作
     *
     * @param request request
     * @return t
     */
    protected Request wrap(Request<?> request) {
        Assert.notNull(request.getBody(), "消息体不能为空");
        if (!Objects.nonNull(request.getServiceId())) {
            request.setServiceId(SpringUtil.getServiceId());
        }
        if (!Objects.nonNull(request.getRequestId())) {
            request.setRequestId(UUID.randomUUID().toString().replaceAll("-", ""));
        }
        if (null == request.getDeliverNum()) {
            request.setDeliverNum(1);
        }
        if (null != request.getDelayMsgHandler()) {
            Assert.isTrue(!(null == request.getDelayTime() || 0 >= request.getDelayTime()), "delayTime is null or zreo");
        }
        return request;
    }
}
