package com.github.likavn.notify.domain;

import com.github.likavn.notify.api.DelayMsgListener;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 通知消息体
 *
 * @author likavn
 * @since 2023/01/01
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("all")
public class MetaRequest<T> implements Message<T>, Serializable {
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
     * 消息投递次数
     */
    private Integer deliverNumber;

    /**
     * 消息体，必须包含无参构造函数
     */
    private T body;
    /**
     * 延时消息处理对象
     */
    private Class<? extends DelayMsgListener> handler;
    /**
     * 消息体
     */
    private Class<?> bodyClass;

    @Builder
    public MetaRequest(Class<? extends DelayMsgListener> handler, String code, T body, Integer deliverNumber) {
        this.handler = handler;
        this.code = code;
        this.body = body;
        this.deliverNumber = deliverNumber;
    }

    @Override
    public String getServiceId() {
        return this.serviceId;
    }

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public int getDeliverNum() {
        return null == this.deliverNumber ? 1 : this.deliverNumber;
    }

    @Override
    public T getBody() {
        return this.body;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setDeliverNumber(Integer deliverNumber) {
        this.deliverNumber = deliverNumber;
    }

    public void setBody(T body) {
        this.body = body;
    }

    public void setHandler(Class<? extends DelayMsgListener> handler) {
        this.handler = handler;
    }

    public void setBodyClass(Class<?> bodyClass) {
        this.bodyClass = bodyClass;
    }

    public String getRequestId() {
        return requestId;
    }

    public Integer getDeliverNumber() {
        return deliverNumber;
    }

    public Class<? extends DelayMsgListener> getHandler() {
        return handler;
    }

    public Class<?> getBodyClass() {
        return bodyClass;
    }
}
