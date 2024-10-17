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
package com.github.likavn.eventbus.core.utils;


import lombok.experimental.UtilityClass;

import java.util.Collection;
import java.util.Map;

/**
 * Assert
 *
 * @author likavn
 * @date 2024/01/01
 **/
@UtilityClass
public class Assert {
    /**
     * 抛出异常
     *
     * @param message 异常信息
     */
    public void throwsException(String message) {
        throw new IllegalArgumentException(message);
    }

    /**
     * 判断条件是否为真，如果不是真则抛出非法参数异常
     *
     * @param expression 判断条件
     * @param message    异常信息
     * @param params     异常信息
     */
    public void isTrue(boolean expression, String message, Object... params) {
        if (!expression) {
            throw new IllegalArgumentException(String.format(message, params));
        }
    }

    public void isTrue(boolean expression, String message) {
        isTrue(expression, message, "");
    }

    /**
     * 判断对象是否为null
     *
     * @param object  待判断对象
     * @param message 异常信息
     * @throws IllegalArgumentException 如果对象不为null
     */
    public void isNull(Object object, String message) {
        if (object != null) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 检查给定的对象是否不为空
     * 如果对象为空，则抛出 IllegalArgumentException 异常
     *
     * @param object  需要检查的对象
     * @param message 异常信息
     * @throws IllegalArgumentException 如果对象为空
     */
    public void notNull(Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 检查字符串不为空
     *
     * @param str     字符串
     * @param message 消息
     * @throws IllegalArgumentException 如果数组为空或数组中存在空元素
     */
    public void notEmpty(String str, String message) {
        if (Func.isEmpty(str)) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 检查数组不为空
     *
     * @param array   数组
     * @param message 消息
     * @throws IllegalArgumentException 如果数组为空或数组中存在空元素
     */
    public void notEmpty(Object[] array, String message) {
        if (Func.isEmpty(array)) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 检查集合不能为空
     *
     * @param collection 集合
     * @param message    异常信息
     * @throws IllegalArgumentException 如果集合为空，抛出异常
     */
    public void notEmpty(Collection<?> collection, String message) {
        if (Func.isEmpty(collection)) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 检查Map不为空
     *
     * @param map     Map对象
     * @param message 错误信息
     * @throws IllegalArgumentException 如果Map为空，抛出此异常
     */
    public void notEmpty(Map<?, ?> map, String message) {
        if (Func.isEmpty(map)) {
            throw new IllegalArgumentException(message);
        }
    }
}
