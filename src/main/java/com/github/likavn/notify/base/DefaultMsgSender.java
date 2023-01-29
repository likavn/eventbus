package com.github.likavn.notify.base;

import com.github.likavn.notify.api.DelayMsgListener;
import com.github.likavn.notify.api.MsgSender;
import com.github.likavn.notify.domain.MetaRequest;
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
public abstract class DefaultMsgSender implements MsgSender {

    /**
     * 发送消息前置操作
     *
     * @param request request
     * @return t
     */
    @SuppressWarnings("all")
    protected MetaRequest before(MetaRequest<?> request) {
        Assert.notNull(request.getBody(), "消息体不能为空");
        if (!Objects.nonNull(request.getServiceId())) {
            request.setServiceId(SpringUtil.getServiceId());
        }
        if (!Objects.nonNull(request.getRequestId())) {
            request.setRequestId(UUID.randomUUID().toString().replaceAll("-", ""));
        }
        if (null == request.getDeliverNumber()) {
            request.setDeliverNumber(1);
        }
        request.setBodyClass(request.getBody().getClass());
        return request;
    }

    protected MetaRequest<?> before(String serviceId, String code, Object body) {
        return before(MetaRequest.builder().serviceId(serviceId).code(code).body(body).build());
    }

    @SuppressWarnings("all")
    protected MetaRequest<?> before(Class<? extends DelayMsgListener> handler, String code, Object body, Integer deliverNumber) {
        return before(MetaRequest.builder().handler(handler).code(code).body(body).deliverNumber(deliverNumber).build());
    }
}
