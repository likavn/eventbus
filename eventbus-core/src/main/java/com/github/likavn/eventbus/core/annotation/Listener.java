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
 * 及时消息订阅注解
 *
 * @author likavn
 * @date 2024/01/01
 **/
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Listener {

    /**
     * 消息所属来源服务ID或服务名。默认订阅本服务{@link BusConfig#getServiceId()}配置的ID
     *
     * @see BusConfig#getServiceId()
     */
    String serviceId() default "";

    /**
     * 消息类型，用于区分不同的消息类型。
     */
    String[] codes();

    /**
     * 定义并发级别，默认值为-1。
     *
     * @return 返回并发级别的整数值。设置-1表示未设置，默认{@link BusConfig#getConcurrency()}。
     */
    int concurrency() default -1;

    /**
     * 消息投递失败异常处理注解
     */
    Fail fail() default @Fail();
}
