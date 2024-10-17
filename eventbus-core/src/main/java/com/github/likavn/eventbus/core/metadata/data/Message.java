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

/**
 * 通知消息体
 *
 * @author likavn
 * @date 2024/01/01
 */
public interface Message<T> {
    /**
     * 获取消息ID
     *
     * @return 消息ID
     */
    String getRequestId();

    /**
     * 消息所属来源服务ID,服务名
     *
     * @return 应用服务ID
     */
    String getServiceId();

    /**
     * 消息编码code，用于区分不同的消息类型
     *
     * @return 消息类型
     */
    String getCode();

    /**
     * 获取消息投递次数
     *
     * @return 消息投递次数
     */
    int getDeliverCount();

    /**
     * 获取消息轮询次数
     *
     * @return 消息轮询次数
     */
    int getPollingCount();

    /**
     * 获取消费者接收失败后，发起的失败重试次数
     *
     * @return 返回重试的次数
     */
    int getFailRetryCount();

    /**
     * 是否为重试消息
     *
     * @return 是否为重试消息
     */
    boolean isRetry();

    /**
     * 获取消息体
     *
     * @return 消息体
     */
    T getBody();
}
