package com.github.likavn.eventbus.core.metadata.data;

import com.github.likavn.eventbus.core.api.MsgDelayListener;
import com.github.likavn.eventbus.core.utils.Func;
import lombok.*;


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
public class Request<T> extends Topic implements Message<T> {
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
    private Class<? extends MsgDelayListener> delayMsgHandler;

    /**
     * 延时时间，单位：秒
     */
    private Long delayTime;

    /**
     * 原消息是否为订阅消息
     */
    private Boolean isOrgSub;

    @Builder
    public Request(Class<? extends MsgDelayListener> delayMsgHandler,
                   String requestId, String serviceId, String code, T body, Integer deliverNum, Boolean isOrgSub, Long delayTime) {
        super(serviceId, code);
        this.delayMsgHandler = delayMsgHandler;
        this.requestId = requestId;
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

    @Override
    public String getTopic() {
        return Func.getTopic(serviceId, code);
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

    public Class<? extends MsgDelayListener> getDelayMsgHandler() {
        return delayMsgHandler;
    }
}
