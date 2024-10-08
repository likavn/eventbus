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
 * 业务消息体编码接口
 * <p>
 * 发送消息时避免每次都要写code编码
 * 备注：实现类必须存在无参构造器
 *
 * @author likavn
 * @date 2024/04/19
 */
public interface MsgBody {

    /**
     * 消息体code
     *
     * @return code编码
     */
    default String code() {
        return this.getClass().getSimpleName();
    }
}
