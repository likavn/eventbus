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
package com.github.likavn.eventbus.core.base;

import com.github.likavn.eventbus.core.metadata.support.Listener;

import java.util.List;

/**
 * 获取消息监听器列表
 *
 * @author likavn
 * @date 2024/5/15
 */
public interface AcquireListeners<T extends Listener> {

    /**
     * 获取消费者
     *
     * @return 消费者
     */
    List<T> getListeners();
}
