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
package com.github.likavn.eventbus.core.support.spi;

import com.github.likavn.eventbus.core.utils.Func;

import java.lang.reflect.Type;

/**
 * json工具SPI
 *
 * @author likavn
 * @date 2024/04/19
 * @since 2.2
 */
public interface IJson {
    String PATTERN_JSON = "(\\{.*\\}|\\[.*\\])";

    /**
     * 判断字符串是否是json串
     *
     * @param val json
     * @return true是
     */
    default boolean isJson(String val) {
        if (Func.isEmpty(val)) {
            return false;
        }
        return val.matches(PATTERN_JSON);
    }

    /**
     * 用于判断是否可用
     *
     * @return true可用
     */
    boolean active();

    /**
     * to json string
     *
     * @param value v
     * @return json str
     */
    String toJsonString(Object value);

    /**
     * json 转对象
     *
     * @param text text
     * @param type to bean class
     * @return bean
     */
    <T> T parseObject(String text, Type type);

    /**
     * 当存在多个可用的json工具时，优先使用order最小的
     *
     * @return order 顺序
     */
    int getOrder();
}
