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
package com.github.likavn.eventbus.core.constant;

import com.github.likavn.eventbus.core.api.MsgDelayListener;
import com.github.likavn.eventbus.core.api.MsgListener;
import com.github.likavn.eventbus.core.metadata.data.Message;

/**
 * eventbus常量
 *
 * @author likavn
 * @date 2024/01/01
 */
public class BusConstant {
    private BusConstant() {
    }

    /**
     * 接口订阅器接收方法名
     *
     * @see MsgListener#onMessage(Message)
     * @see MsgDelayListener#onMessage(Message)
     */
    public static final String ON_MESSAGE = "onMessage";

    /**
     * 接口订阅器接收异常的方法名
     *
     * @see MsgListener#failHandler(Message, Throwable)
     * @see MsgDelayListener#failHandler(Message, Throwable)
     */
    public static final String FAIL_HANDLER = "failHandler";

    /**
     * thread name
     */
    public static final String TASK_NAME = "eventbus-task-pool-";

    /**
     * subscribe thread name
     */
    public static final String THREAD_NAME = "eventbus-msg-pool-";

    /**
     * 配置前缀
     */
    public static final String CONFIG_PREFIX = "eventbus";

    /**
     * 消息引擎配置key
     */
    public static final String TYPE_NAME = "type";

    /**
     * 迁移原有消息引擎配置key
     */
    public static final String OLD_TYPE_NAME = "oldType";

    public static final String REDIS = "redis";
    public static final String RABBITMQ = "rabbitmq";
    public static final String ROCKETMQ = "rocketmq";
}
