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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * topic
 *
 * @author likavn
 * @date 2024/01/01
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class Topic implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 消息所属来源服务ID,服务名
     */
    protected String serviceId;

    /**
     * 消息类型，用于区分不同的消息类型
     */
    protected String code;

    /**
     * 获取topic
     *
     * @return topic
     */
    public abstract String getTopic();
}
