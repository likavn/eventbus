package com.github.likavn.notify.domain;

import com.github.likavn.notify.api.SubscribeMsgListener;
import com.github.likavn.notify.utils.WrapUtils;
import lombok.Builder;
import lombok.Data;

/**
 * 消息订阅监听器消费者实体数据
 *
 * @author likavn
 * @date 2023/1/7
 **/
@Data
@Builder
public final class SubMsgConsumer {
    /**
     * 监听器
     */
    private SubscribeMsgListener<?> listener;

    /**
     * 消费者数量
     */
    private int consumerNum = 2;

    /**
     * 消息所属来源服务ID,服务名
     */
    private String serviceId;

    /**
     * 消息类型，用于区分不同的消息类型
     */
    private String code;

    public String getTopic() {
        return serviceId + "|" + code;
    }

    /**
     * 数据接收
     */
    @SuppressWarnings("all")
    public void accept(byte[] body) {
        listener.receiver(WrapUtils.convertByBytes(body));
    }

    /**
     * 数据接收
     *
     * @param message
     */
    @SuppressWarnings("all")
    public void accept(Message message) {
        listener.receiver(message);
    }
}
