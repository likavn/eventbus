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
import com.github.likavn.eventbus.core.metadata.data.MsgBody;

import java.lang.annotation.*;

/**
 * 消息监听/订阅器注解
 * <p>
 * 用于标记对及时/延时消息的监听类，可以通过此注解定制消息处理的详细行为，如所属服务、消息类型和并发级别等。
 *
 * @author likavn
 * @date 2024/07/27
 * @since 2.5
 **/
@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface EventbusListener {

    /**
     * 消息所属来源服务ID或服务名。默认订阅本服务{@link BusConfig#getServiceId()}配置的ID
     *
     * @see BusConfig#getServiceId()
     */
    String serviceId() default "";

    /**
     * 监听器订阅的消息类型，用于区分不同的消息类型。
     * <p>
     * 为空时：
     * 1.如果消息实体不继承接口{@link MsgBody}，则默认为当前监听器的类名。
     * 2.如果消息实体继承接口{@link MsgBody}，则默认为{@link MsgBody#code()}。
     * <p>
     * 监听器订阅code优先如下：
     *
     * @see #codes
     * @see MsgBody#code()
     * 当前监听器类名
     */
    String[] codes() default {};

    /**
     * 消息接收并发数，默认值为-1。
     *
     * @return 返回并发级别的整数值。设置-1表示未设置，默认{@link BusConfig#getConcurrency()}。
     */
    int concurrency() default -1;

    /**
     * 重试消息接收并发数，默认值为-1。
     *
     * @return 返回并发级别的整数值。设置-1表示未设置，默认{@link BusConfig#getRetryConcurrency()} ()}。
     */
    int retryConcurrency() default -1;
}
