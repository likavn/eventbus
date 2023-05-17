package com.github.likavn.notify.domain;

import com.github.likavn.notify.api.DelayMsgListener;
import lombok.*;

import java.io.Serializable;

/**
 * 通知消息体
 *
 * @author likavn
 * @since 2023/01/01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("all")
@EqualsAndHashCode(callSuper = true)
public class Request<T> extends Topic implements Message<T>, Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 事件ID
     */
    private String requestId;

    /**
     * 消息投递次数
     */
    private Integer deliverNum;

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

    /**
     * 延时时间，单位：秒
     */
    private Long delayTime;

    /**
     * 原消息是否为订阅消息
     */
    private Boolean isOrgSub;

    @Builder
    public Request(Class<? extends DelayMsgListener> handler, String serviceId, String code, T body, Integer deliverNum, Boolean isOrgSub, Long delayTime) {
        super(serviceId, code);
        this.handler = handler;
        this.body = body;
        this.deliverNum = deliverNum;
        this.delayTime = delayTime;
        this.isOrgSub = (null == isOrgSub ? Boolean.FALSE : isOrgSub);
    }

    @Override
    public String getRequestId() {
        return requestId;
    }

    @Override
    public Integer getDeliverNum() {
        return null == this.deliverNum ? 1 : this.deliverNum;
    }

    @Override
    public T getBody() {
        return this.body;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public void setDeliverNum(Integer deliverNum) {
        this.deliverNum = deliverNum;
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

    public Class<? extends DelayMsgListener> getHandler() {
        return handler;
    }

    public Class<?> getBodyClass() {
        return bodyClass;
    }
}
