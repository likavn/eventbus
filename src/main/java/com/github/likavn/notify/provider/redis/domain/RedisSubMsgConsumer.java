package com.github.likavn.notify.provider.redis.domain;

import com.github.likavn.notify.domain.SubMsgConsumer;
import com.github.likavn.notify.provider.redis.constant.RedisConstant;

/**
 * redis消息订阅监听器消费者实体数据
 *
 * @author likavn
 * @date 2023/1/7
 **/
public final class RedisSubMsgConsumer extends SubMsgConsumer {

    /**
     * 消费者监听stream key
     */
    private final String key;

    /**
     * 消费者所在消费者组
     */
    private final String group;

    public RedisSubMsgConsumer(SubMsgConsumer subMsgConsumer) {
        super(subMsgConsumer.getListener(),
                subMsgConsumer.getConsumerNum(), subMsgConsumer.getServiceId(), subMsgConsumer.getCode());
        this.key = String.format(RedisConstant.NOTIFY_SUBSCRIBE_PREFIX, subMsgConsumer.getTopic());
        this.group = subMsgConsumer.getListener().getClass().getName();
    }

    public String getKey() {
        return key;
    }

    public String getGroup() {
        return group;
    }
}
