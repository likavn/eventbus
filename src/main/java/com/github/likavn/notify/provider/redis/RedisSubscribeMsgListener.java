package com.github.likavn.notify.provider.redis;

import com.github.likavn.notify.domain.MetaRequest;
import com.github.likavn.notify.domain.SubMsgConsumer;
import com.github.likavn.notify.prop.NotifyProperties;
import com.github.likavn.notify.provider.redis.constant.RedisConstant;
import com.github.likavn.notify.utils.WrapUtils;
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
    private final NotifyProperties properties;
    /**
     * 消费者
     */
    private final SubMsgConsumer consumer;

    private final RLock rLock;

    public RedisSubscribeMsgListener(NotifyProperties properties, SubMsgConsumer consumer, RLock rLock) {
        this.properties = properties;
        this.consumer = consumer;
        this.rLock = rLock;
    }

    @Override
    @SuppressWarnings("all")
    public void onMessage(Message message, byte[] pattern) {
        MetaRequest req = WrapUtils.convertByBytes(message.getBody());
        boolean getLock = false;
        try {
            // 添加分布式锁，防止消息重复投放
            getLock = rLock.getLock(RedisConstant.REDIS_LOCK_KEY
                    + req.getRequestId(), properties.getRedis().getLockTimeout());
            if (getLock) {
                consumer.accept(req);
            }
        } finally {
            if (getLock) {
                rLock.releaseLock(req.getRequestId());
            }
        }
    }
}
