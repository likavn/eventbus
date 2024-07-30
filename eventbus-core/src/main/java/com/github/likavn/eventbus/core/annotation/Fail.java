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

import com.github.likavn.eventbus.core.metadata.BusConfig;

import java.lang.annotation.*;

/**
 * tips: 消息投递失败异常处理注解，
 * 使用在消息重试投递最终失败时进行回调。必须和订阅器在同一个类中
 *
 * @author likavn
 * @date 2024/01/01
 */
@Documented
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Fail {
    /**
     * 失败回调方法，回调方法必须和订阅器处理器在同一个类中,最后一次重试投递仍然抛出异常时会调用此方法
     * <p>回调方法可选参数（无序）：</p>
     * 1：消息体{@link com.github.likavn.eventbus.core.metadata.data.Message}
     * 1：重复投递失败异常，为原始异常{@link java.lang.Throwable}
     */
    String callMethod() default "";

    /**
     * 消息投递失败时，一定时间内再次进行投递的次数
     * <code>retryCount < 0</code> 时根据全局配置{@link BusConfig.Fail#getRetryCount()} 默认为3次
     */
    int retryCount() default -1;

    /**
     * 投递失败时，下次下次投递触发的间隔时间,单位：秒
     * <code>nextTime <= 0</code>时根据全局配置{@link BusConfig.Fail#getNextTime()} 默认为10秒
     */
    long nextTime() default -1L;
}
