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

import com.github.likavn.eventbus.core.exception.EventBusException;

/**
 * 中间件实现监听容器初始化接口
 * <p>
 * 该接口定义了中间件在容器初始化过程中需要实现的生命周期方法
 * 主要包括组件注册和组件销毁两个重要环节
 *
 * @author likavn
 * @date 2024/01/01
 **/
public interface Lifecycle {

    /**
     * 监听组件注册
     * <p>
     * 该方法用于在容器初始化时注册中间件的监听器
     * 可能会抛出与事件总线相关的异常
     *
     * @throws EventBusException e
     */
    void register() throws EventBusException;

    /**
     * 监听组件销毁
     * <p>
     * 该方法用于在容器关闭时销毁中间件的监听器
     * 可能会抛出与事件总线相关的异常
     *
     * @throws EventBusException e
     */
    void destroy() throws EventBusException;
}
