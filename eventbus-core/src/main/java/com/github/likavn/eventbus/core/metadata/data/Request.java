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

import com.github.likavn.eventbus.core.metadata.MsgType;
import com.github.likavn.eventbus.core.metadata.support.Trigger;
import com.github.likavn.eventbus.core.utils.Func;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * 通知消息体，eventbus原始消息体
 *
 * @author likavn
 * @date 2024/01/01
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
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
    private int deliverCount;

    /**
     * 消费者轮询次数
     */
    private int pollingCount;

    /**
     * 消费者接收失败后时，发起失败重试的次数
     */
    private int failRetryCount;

    /**
     * 消息类型,默认及时消息
     */
    private MsgType type;

    /**
     * 延时消息的延时时间，单位：秒
     */
    private long delayTime;

    /**
     * 及时消息是否已转为延迟消息
     */
    private boolean toDelay;

    /**
     * 是否为消息重发
     */
    private boolean retry;

    /**
     * 消息头
     */
    private Map<String, String> headers;

    /**
     * 业务消息体
     * 注：必须包含无参构造函数
     */
    @SuppressWarnings("all")
    private T body;

    @Override
    public String topic() {
        return Func.getTopic(serviceId, code);
    }

    /**
     * 将当前对象转换为JSON字符串
     */
    public String toJson() {
        return Func.toJson(this);
    }

    /**
     * 添加消息头
     */
    public void addHeader(String key, String value) {
        if (headers == null) {
            headers = new HashMap<>(4);
        }
        headers.put(key, value);
    }

    /**
     * 获取消息头
     */
    public String header(String key) {
        return headers == null ? null : headers.get(key);
    }
}
