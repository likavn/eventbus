package com.github.likavn.notify.domain;

import com.github.likavn.notify.base.BaseSubscribeMsgListener;
import lombok.Builder;
import lombok.Data;


/**
 * @author likavn
 * @date 2023/1/7
 **/
@Data
@Builder
public class SubMsgListener {
    /**
     * 监听器
     */
    private BaseSubscribeMsgListener<?> listener;

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
}
