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
import com.github.likavn.eventbus.core.utils.Func;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;

/**
 * 接收器容器
 *
 * @author likavn
 * @date 2024/1/23
 **/
@Slf4j
public class MsgListenerContainer {
    private final Collection<Lifecycle> listeners;
    protected volatile boolean active = false;

    public MsgListenerContainer(Collection<Lifecycle> listeners) {
        this.listeners = listeners;
    }

    /**
     * 启动检测状态
     */
    public void startup() {
        if (Func.isEmpty(listeners)) {
            return;
        }
        try {
            this.active = true;
            registerListeners();
        } catch (Exception e) {
            log.error("eventbus startup", e);
            throw new EventBusException(e);
        }
    }

    /**
     * 停止检测状态
     */
    public void shutdown() {
        try {
            this.active = false;
            this.destroyListeners();
        } catch (Exception e) {
            log.error("eventbus shutdown", e);
            throw new EventBusException(e);
        }
    }

    /**
     * 获取当前监听组件状态
     */
    public boolean isActive() {
        return active;
    }

    /**
     * 注册所有监听组件
     */
    protected void registerListeners() {
        if (!this.active) {
            return;
        }
        // 遍历组件列表
        for (Lifecycle listener : listeners) {
            // 注册组件
            listener.register();
        }
        log.info("Eventbus register listeners success");
    }

    /**
     * 销毁所有监听组件
     */
    protected void destroyListeners() {
        // 遍历组件列表
        for (Lifecycle listener : listeners) {
            // 销毁组件
            listener.destroy();
        }
        log.info("Eventbus destroy listeners...");
    }
}
