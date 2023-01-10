package com.github.likavn.notify.provider.redis;

import com.github.likavn.notify.domain.SubMsgListener;
import com.github.likavn.notify.utils.WrapUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;

/**
 * @author likavn
 * @since 2023/01/01
 **/
@Slf4j
public class RedisSubscribeMsgListener implements MessageListener {

    private final SubMsgListener subMsgListener;

    public RedisSubscribeMsgListener(SubMsgListener subMsgListener) {
        this.subMsgListener = subMsgListener;
    }

    @Override
    @SuppressWarnings("all")
    public void onMessage(Message message, byte[] pattern) {
        subMsgListener.getListener().receiverDelivery(WrapUtils.convertByBytes(message.getBody()));
    }
}
