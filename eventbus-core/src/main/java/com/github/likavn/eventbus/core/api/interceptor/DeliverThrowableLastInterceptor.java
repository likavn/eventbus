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
package com.github.likavn.eventbus.core.api.interceptor;

import com.github.likavn.eventbus.core.metadata.data.Request;

/**
 * 投递异常，全局拦截器
 * 注：消息重试投递都失败时，最后一次消息投递任然失败时会调用该拦截器
 *
 * @author likavn
 * @date 2024/01/01
 **/
public interface DeliverThrowableLastInterceptor {

    /**
     * 拦截器执行
     *
     * @param request   request
     * @param throwable t
     */
    void execute(Request<String> request, Throwable throwable);
}
