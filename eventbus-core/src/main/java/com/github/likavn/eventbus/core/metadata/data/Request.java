package com.github.likavn.eventbus.core.metadata.data;

import com.github.likavn.eventbus.core.api.MsgDelayListener;
import com.github.likavn.eventbus.core.metadata.MsgType;
import com.github.likavn.eventbus.core.utils.Func;
import lombok.*;


/**
 * 通知消息体
 *
 * @author likavn
 * @date 2024/01/01
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
     * 消息接收处理器ID=全类名+方法名
     */
    private String deliverId;

    /**
     * 消息投递次数
     */
    private Integer deliverNum;

    /**
     * 消息体，必须包含无参构造函数
     */
    private T body;

    /**
     * 延时消息处理器
     */
    private Class<? extends MsgDelayListener> delayListener;

    /**
     * 延时时间，单位：秒
     */
    private Long delayTime;

    /**
     * 消息类型,默认及时消息
     */
    private MsgType type = MsgType.TIMELY;

    @Builder
    public Request(Class<? extends MsgDelayListener> delayListener,
                   String requestId, String serviceId, String code, T body, Integer deliverNum, MsgType type, Long delayTime) {
        super(serviceId, code);
        this.delayListener = delayListener;
        this.requestId = requestId;
        this.body = body;
        this.deliverNum = deliverNum;
        this.delayTime = delayTime;
        this.type = (null == type ? MsgType.TIMELY : type);
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

    public Class<? extends MsgDelayListener> getDelayListener() {
        return delayListener;
    }
}
