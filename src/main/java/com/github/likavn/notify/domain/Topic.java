package com.github.likavn.notify.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * topic
 *
 * @author likavn
 * @date 2023/1/7
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Topic {

    /**
     * 消息所属来源服务ID,服务名
     */
    private String serviceId;

    /**
     * 消息类型，用于区分不同的消息类型
     */
    private String code;

    public String getTopic() {
        return serviceId + "." + code;
    }
}
