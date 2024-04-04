/**
 * Copyright 2023-2033, likavn (likavn@163.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.likavn.eventbus.core.metadata.data;

import com.alibaba.fastjson2.annotation.JSONField;
import com.github.likavn.eventbus.core.api.MsgDelayListener;
import com.github.likavn.eventbus.core.constant.BusConstant;
import com.github.likavn.eventbus.core.metadata.MsgType;
import com.github.likavn.eventbus.core.metadata.support.Trigger;
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
     * 事件ID,默认UUID
     */
    private String requestId;

    /**
     * 消息接收处理器（消费者ID）ID=全类名+方法名{@link Trigger#getDeliverId()}
     */
    private String deliverId;

    /**
     * 消息投递次数
     */
    private Integer deliverCount;

    /**
     * 消息体，必须包含无参构造函数
     */
    private T body;

    /**
     * 延时时间，单位：秒
     */
    private Long delayTime;

    /**
     * 消息类型,默认及时消息
     */
    private MsgType type;

    @Builder
    public Request(Class<? extends MsgDelayListener> delayListener,
                   String requestId, String serviceId, String deliverId, String code, T body, Integer deliverCount, MsgType type, Long delayTime) {
        super(serviceId, code);
        this.requestId = requestId;
        this.deliverId = deliverId;
        if (null != delayListener) {
            deliverId = Func.getDeliverId(delayListener, BusConstant.ON_MESSAGE);
            if (!Func.isEmpty(this.deliverId)) {
                this.deliverId = deliverId + "," + this.deliverId;
            } else {
                this.deliverId = deliverId;
            }
        }
        this.body = body;
        this.deliverCount = deliverCount;
        this.type = type;
        this.delayTime = delayTime;
    }

    @Override
    public String getRequestId() {
        return requestId;
    }

    @Override
    public Integer getDeliverCount() {
        return null == this.deliverCount ? 1 : this.deliverCount;
    }

    @Override
    public T getBody() {
        return this.body;
    }

    @Override
    @JSONField(serialize = false)
    public String getTopic() {
        return Func.getTopic(serviceId, code);
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public void setDeliverCount(Integer deliverCount) {
        this.deliverCount = deliverCount;
    }

    public void setBody(T body) {
        this.body = body;
    }
}
