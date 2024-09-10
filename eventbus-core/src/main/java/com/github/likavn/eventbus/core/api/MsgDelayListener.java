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
 * 延时消息监听器
 *
 * @param <T> 消息体的数据类型
 * @author likavn
 * @date 2024/01/01
 */
public interface MsgDelayListener<T> {

    /**
     * 处理器
     *
     * @param message 消息体，包含延时消息的数据和元信息
     */
    void onMessage(Message<T> message);
}
