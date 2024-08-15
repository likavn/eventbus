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
package com.github.likavn.eventbus.core.annotation;

import com.github.likavn.eventbus.core.metadata.data.Message;

import java.lang.annotation.*;

/**
 * ToDelay 注解用于接收异步消息的方法上，使得当前消息转成延时消息
 *
 * @author likavn
 * @date 2024/07/27
 * @since 2.3.4
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ToDelay {

    /**
     * 延迟时间,单位：秒
     * 该方法用于定义 ToDelay 注解的延迟时间属性
     *
     * @return 延迟时间 返回延迟执行的时间，单位为秒
     */
    long delayTime();

    /**
     * 是否需要首次执行
     * 该方法用于定义 ToDelay 注解的延迟时间属性，
     * 默认：false (第一次获取到消息时不执行接收方法:{@link com.github.likavn.eventbus.core.api.MsgListener#onMessage(Message)})
     *
     * @return 第一次是否接收执行
     */
    boolean firstDeliver() default false;
}

