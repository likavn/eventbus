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
 * 并发级别注解，优先级最高
 *
 * @author likavn
 * @date 2023/7/19
 * @since 2.3.4
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Concurrency {
    /**
     * 定义并发级别，默认值为-1。
     *
     * @return 返回并发级别的整数值。设置-1表示未设置，默认{@link BusConfig#getConcurrency()}。
     */
    int concurrency() default -1;
}
