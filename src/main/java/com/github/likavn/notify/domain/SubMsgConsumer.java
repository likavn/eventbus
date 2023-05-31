package com.github.likavn.notify.domain;

import com.github.likavn.notify.api.SubscribeMsgListener;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 消息订阅监听器消费者实体数据
 *
 * @author likavn
 * @date 2023/1/7
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class SubMsgConsumer extends Topic {
    /**
     * 监听器
     */
    private SubscribeMsgListener<?> listener;

    /**
     * 消费者数量
     */
    private Integer consumerNum;

    @Builder
    public SubMsgConsumer(SubscribeMsgListener<?> listener, Integer consumerNum, String serviceId, String code) {
        super(serviceId, code);
        this.listener = listener;
        this.consumerNum = consumerNum;
    }

    /**
     * 数据接收
     */
    @SuppressWarnings("all")
    public void accept(byte[] body) {
        listener.deliver(body);
    }

    /**
     * 数据接收
     */
    @SuppressWarnings("all")
    public void accept(String body) {
        listener.deliver(body);
    }
}
