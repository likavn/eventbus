package com.github.likavn.notify.provider.redis;

import com.github.likavn.notify.domain.SubMsgConsumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;

/**
 * redis消息监听器
 *
 * @author likavn
 * @since 2023/01/01
 **/
@Slf4j
public class RedisSubscribeMsgListener implements MessageListener {

    private final SubMsgConsumer consumer;

    public RedisSubscribeMsgListener(SubMsgConsumer consumer) {
        this.consumer = consumer;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        consumer.accept(message.getBody());
    }
}
