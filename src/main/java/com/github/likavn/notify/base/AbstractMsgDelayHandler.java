package com.github.likavn.notify.base;

import com.github.likavn.notify.api.DelayMsgListener;
import com.github.likavn.notify.domain.Message;
import com.github.likavn.notify.domain.Request;
import com.github.likavn.notify.prop.NotifyProperties;
import com.github.likavn.notify.utils.SpringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;

/**
 * 延时消息处理类
 *
 * @author likavn
 * @since 2023/01/01
 */
@SuppressWarnings("all")
public abstract class AbstractMsgDelayHandler extends AbstractMsgFailRetryHandler implements MsgListenerContainer {
    private static final Logger logger = LoggerFactory.getLogger(AbstractMsgDelayHandler.class);

    @Resource
    private NotifyProperties properties;

    public AbstractMsgDelayHandler() {
        super(null, null);
    }

    @Override
    public void accept(Message message) {
        Request<?> request = (Request<?>) message;
        DelayMsgListener listener = SpringUtil.getBean(request.getDelayMsgHandler());
        if (null != listener) {
            if (logger.isDebugEnabled()) {
                logger.debug("[延时消息]接收延时回调数据requestId={}", message.getRequestId());
            }
            listener.onMessage(message);
        } else {
            logger.error("[延时消息]不存在消息处理器requestId={}", message.getRequestId());
        }
    }
}
