package com.github.likavn.notify.base;

import com.github.likavn.notify.api.DelayMsgListener;
import com.github.likavn.notify.domain.MsgRequest;
import com.github.likavn.notify.utils.SpringUtil;
import com.github.likavn.notify.utils.WrapUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 延时消息处理类
 *
 * @author likavn
 * @since 2023/01/01
 */
@Slf4j
public abstract class BaseDelayMsgListener {

    /**
     * 处理
     */
    @SuppressWarnings("all")
    protected void handler(MsgRequest<?> event) {
        DelayMsgListener delayMessageHandler = SpringUtil.getBean(event.getHandler());
        if (null != delayMessageHandler) {
            log.info("接收延时回调数据={}", WrapUtils.toJson(event));
            if (null == event.getBody()) {
                return;
            }
            delayMessageHandler.onMessage(event);
        } else {
            log.error("[延时队列]不存在消息处理器，event={}", WrapUtils.toJson(event));
        }
    }

}
