package com.github.likavn.notify.domain;

import com.github.likavn.notify.api.DelayMsgListener;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 通知消息体
 * 注意：变更当前实体数据结构，请升级rabbitMq交换机、路由key、队列名称版本
 * 消息体（body）部分必须包含无参构造函数
 *
 * @author likavn
 * @since 2023/01/01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MsgRequest<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 事件ID
     */
    private String requestId;

    /**
     * 消息所属来源服务ID,服务名
     */
    private String serviceId;

    /**
     * 消息类型，用于区分不同的消息类型
     */
    private String code;

    /**
     * 延时消息处理对象
     */
    private Class<? extends DelayMsgListener> handler;

    /**
     * 处理次数
     */
    private Integer handlerNum;

    /**
     * 消息体，必须包含无参构造函数
     */
    private T body;

    /**
     * 消息体
     */
    private Class<?> bodyClass;

    @Builder
    public MsgRequest(String code, Class<? extends DelayMsgListener> handler, T body, Integer handlerNum) {
        this.code = code;
        this.handler = handler;
        this.body = body;
        this.handlerNum = handlerNum;
    }

    public String getTopic() {
        return serviceId + "|" + code;
    }
}
