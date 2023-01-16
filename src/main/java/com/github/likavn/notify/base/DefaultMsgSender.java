package com.github.likavn.notify.base;

import com.github.likavn.notify.api.MsgSender;
import com.github.likavn.notify.domain.MsgRequest;
import com.github.likavn.notify.utils.SpringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.util.Objects;
import java.util.UUID;

/**
 * 延时消息处理类
 *
 * @author likavn
 * @since 2023/01/01
 */
@Slf4j
public abstract class DefaultMsgSender implements MsgSender {

    /**
     * 发送消息前置操作
     *
     * @param request request
     * @return t
     */
    @SuppressWarnings("all")
    protected void before(MsgRequest<?> request) {
        Assert.notNull(request.getBody(), "消息体不能为空");
        if (!Objects.nonNull(request.getServiceId())) {
            request.setServiceId(SpringUtil.getServiceId());
        }
        if (!Objects.nonNull(request.getRequestId())) {
            request.setRequestId(UUID.randomUUID().toString().replaceAll("-", ""));
        }
        if (null == request.getHandlerNum()) {
            request.setHandlerNum(1);
        }
        request.setBodyClass(request.getBody().getClass());
    }
}
