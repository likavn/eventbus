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

import com.github.likavn.eventbus.core.annotation.SubscribeDelay;
import com.github.likavn.eventbus.core.api.MsgDelayListener;
import com.github.likavn.eventbus.core.metadata.data.Message;
import lombok.extern.slf4j.Slf4j;

/**
 * 默认延时处理器
 * 及时消息重试及{@link SubscribeDelay}注解订阅的消息
 *
 * @author likavn
 * @date 2024/01/01
 **/
@Slf4j
public class DefaultMsgDelayListener implements MsgDelayListener<Object> {

    @Override
    public void onMessage(Message<Object> message) {
        throw new UnsupportedOperationException();
    }
}
