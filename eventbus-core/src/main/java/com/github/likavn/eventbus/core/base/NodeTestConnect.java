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

/**
 * 节点连接状态检测接口，用于判断当前应用是否与节点断开连接
 *
 * @author likavn
 * @date 2024/01/01
 **/
public interface NodeTestConnect {

    /**
     * 检测确认节点是否连接
     *
     * @return true已连接、false连接断开
     */
    boolean testConnect();
}
