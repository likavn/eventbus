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

/**
 * 及时消息监听器
 *
 * @param <T> 消息体的数据类型
 * @author likavn
 * @date 2024/01/01
 */
public interface MsgListener<T> {
    /**
     * 处理器
     * 当有新消息到达时，此方法被调用以处理消息
     *
     * @param message 消息体，包含延时消息的数据和元信息
     */
    void onMessage(Message<T> message);

    /**
     * 处理消息投递异常
     * 当消息投递过程中发生异常时，此方法被调用用于处理该异常
     *
     * @param message   消息，发生异常的消息对象
     * @param throwable 异常，投递过程中遇到的异常对象
     */
    default void failHandler(Message<T> message, Throwable throwable) {
    }
}
