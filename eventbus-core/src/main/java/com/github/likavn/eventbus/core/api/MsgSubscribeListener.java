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
package com.github.likavn.eventbus.core.api;

import com.github.likavn.eventbus.core.metadata.data.Message;

import java.util.Arrays;
import java.util.List;

/**
 * 消息订阅超类
 *
 * @author likavn
 * @date 2024/01/01
 */
public abstract class MsgSubscribeListener<T> {
    /**
     * 消息所属来源服务ID,服务名
     */
    private final String serviceId;

    /**
     * 消息类型，用于区分不同的消息类型
     */
    private final List<String> codes;

    /**
     * 构造器
     *
     * @param codes 消息编码
     */
    protected MsgSubscribeListener(String... codes) {
        this(Arrays.asList(codes));
    }

    /**
     * 构造器
     *
     * @param codes 消息编码
     */
    protected MsgSubscribeListener(List<String> codes) {
        this(null, codes);
    }

    /**
     * 构造器
     *
     * @param serviceId 消息服务的ID
     * @param codes     消息编码
     */
    protected MsgSubscribeListener(String serviceId, List<String> codes) {
        this.serviceId = serviceId;
        this.codes = codes;
    }

    public String getServiceId() {
        return serviceId;
    }

    public List<String> getCodes() {
        return codes;
    }

    /**
     * 处理器
     *
     * @param message 消息体
     */
    public abstract void onMessage(Message<T> message);
}
