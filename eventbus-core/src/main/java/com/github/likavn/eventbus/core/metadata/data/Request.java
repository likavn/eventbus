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

import com.github.likavn.eventbus.core.api.MsgDelayListener;
import com.github.likavn.eventbus.core.constant.BusConstant;
import com.github.likavn.eventbus.core.metadata.MsgType;
import com.github.likavn.eventbus.core.metadata.support.Trigger;
import com.github.likavn.eventbus.core.utils.Func;
import lombok.*;

/**
 * 通知消息体，eventbus原始消息体
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
     * <p>
     * 如需修改请实现此接口{@link com.github.likavn.eventbus.core.api.RequestIdGenerator)}
     */
    private String requestId;

    /**
     * 消息接收处理器（消费者/投递）ID=类完全限定名+方法名{@link Trigger#getDeliverId()}
     */
    private String deliverId;

    /**
     * 消息投递次数
     */
    private Integer deliverCount;

    /**
     * 消费者轮询次数
     */
    private Integer pollingCount;

    /**
     * 消费者接收失败后时，发起失败重试的次数
     */
    private Integer failRetryCount;

    /**
     * 消息类型,默认及时消息
     */
    private MsgType type;

    /**
     * 延时消息的延时时间，单位：秒
     */
    private Long delayTime;

    /**
     * 是否已转为延迟消息
     */
    private Boolean toDelay;

    /**
     * 业务消息体
     * 注：必须包含无参构造函数
     */
    private T body;

    @Builder
    public Request(String serviceId,
                   String code,
                   String requestId,
                   String deliverId,
                   Class<? extends MsgDelayListener> delayListener,
                   Integer deliverCount,
                   MsgType type,
                   Long delayTime,
                   T body) {
        super(serviceId, code);
        this.requestId = requestId;
        this.deliverId = deliverId;
        if (null != delayListener) {
            this.deliverId = Func.getDeliverId(delayListener, BusConstant.ON_MESSAGE);
            setCode(this.deliverId);
        }
        this.deliverCount = deliverCount;
        this.type = type;
        this.delayTime = delayTime;
        this.body = body;
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
    public Integer getPollingCount() {
        return null == this.pollingCount ? 0 : this.pollingCount;
    }

    @Override
    public Integer getFailRetryCount() {
        return null == this.failRetryCount ? 0 : this.failRetryCount;
    }

    @Override
    public T getBody() {
        return this.body;
    }

    @Override
    public String topic() {
        return Func.getTopic(serviceId, code);
    }

    @Override
    public String delayTopic() {
        if (type.isDelay()) {
            return Func.getTopic(serviceId, code);
        }
        return Func.getDelayTopic(serviceId, code, deliverId);
    }
}
