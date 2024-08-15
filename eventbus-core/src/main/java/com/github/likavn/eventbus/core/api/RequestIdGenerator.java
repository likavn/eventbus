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
package com.github.likavn.eventbus.core.api;

/**
 * 请求ID生成接口，须保证ID的唯一性
 * 该接口定义了获取唯一请求ID的方法，用于在分布式系统中唯一标识每个请求
 * 实现该接口的类需要确保生成的请求ID在系统中是唯一的
 *
 * @author likavn
 * @date 2024/4/2
 **/
public interface RequestIdGenerator {

    /**
     * 获取请求ID
     * 该方法用于生成并返回一个唯一的请求ID
     * 实现该方法时，需要确保ID的全局唯一性和生成效率
     *
     * @return 请求ID 由实现类生成的唯一请求ID
     */
    String nextId();
}
