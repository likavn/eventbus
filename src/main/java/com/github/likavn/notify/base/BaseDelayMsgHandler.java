package com.github.likavn.notify.base;

import com.github.likavn.notify.api.DelayMsgListener;
import com.github.likavn.notify.domain.MsgRequest;
import com.github.likavn.notify.utils.SpringUtil;
import com.github.likavn.notify.utils.WrapUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 延时消息处理类
 *
 * @author likavn
 * @since 2023/01/01
 */
@Slf4j
public abstract class BaseDelayMsgHandler {

    /**
     * 延时消息监听器
     */
    @SuppressWarnings("all")
    private static Map<Class<? extends
            DelayMsgListener>, DelayMsgListener> handlerMap = new ConcurrentHashMap<>();

    /**
     * 接收
     *
     * @param body body
     */
    protected void receiver(String body) {
        receiver(WrapUtils.convertByBytes(body));
    }

    /**
     * 接收
     *
     * @param body body
     */
    protected void receiver(byte[] body) {
        receiver(WrapUtils.convertByBytes(body));
    }

    /**
     * 接收
     *
     * @param body body
     */
    @SuppressWarnings("all")
    private void receiver(MsgRequest<?> body) {
        DelayMsgListener listener = handlerMap
                .computeIfAbsent(body.getHandler(), key -> SpringUtil.getBean(body.getHandler()));
        if (null != listener) {
            log.info("[延时消息]接收延时回调数据={}", WrapUtils.toJson(body));
            listener.onMessage(body);
        } else {
            log.error("[延时消息]不存在消息处理器，event={}", WrapUtils.toJson(body));
        }
    }

}
