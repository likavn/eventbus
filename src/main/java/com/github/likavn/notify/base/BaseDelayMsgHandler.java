package com.github.likavn.notify.base;

import com.github.likavn.notify.api.DelayMsgListener;
import com.github.likavn.notify.domain.MetaRequest;
import com.github.likavn.notify.utils.SpringUtil;
import com.github.likavn.notify.utils.WrapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 延时消息处理类
 *
 * @author likavn
 * @since 2023/01/01
 */
public abstract class BaseDelayMsgHandler {
    private static final Logger logger = LoggerFactory.getLogger(BaseDelayMsgHandler.class);

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
    private void receiver(MetaRequest<?> request) {
        DelayMsgListener listener = handlerMap
                .computeIfAbsent(request.getHandler(), key -> SpringUtil.getBean(request.getHandler()));
        if (null != listener) {
            logger.info("[延时消息]接收延时回调数据={}", WrapUtils.toJson(request));
            listener.onMessage(request);
        } else {
            logger.error("[延时消息]不存在消息处理器，event={}", WrapUtils.toJson(request));
        }
    }

}
