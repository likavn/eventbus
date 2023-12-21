package com.github.likavn.eventbus.core.metadata.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * topic
 *
 * @author likavn
 * @date 2023/1/7
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class Topic implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 消息所属来源服务ID,服务名
     */
    protected String serviceId;

    /**
     * 消息类型，用于区分不同的消息类型
     */
    protected String code;

    /**
     * 获取topic
     *
     * @return topic
     */
    public abstract String getTopic();
}
