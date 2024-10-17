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
package com.github.likavn.eventbus.provider;

import com.github.likavn.eventbus.core.exception.EventBusException;
import com.github.likavn.eventbus.core.utils.Func;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.function.Function;

/**
 * 泛型函数接口，获取字段名
 *
 * @author likavn
 * @date 2024/10/01
 */
public interface SFieldFunction<T, R> extends Function<T, R>, Serializable {

    /**
     * 获取字段名
     *
     * @return 字段名
     */
    @SuppressWarnings("all")
    default String getFieldName() {
        try {
            // 获取当前实例的writeReplace方法，该方法是获取SerializedLambda的关键
            Method method = this.getClass().getDeclaredMethod("writeReplace");
            method.setAccessible(true);
            SerializedLambda serializedLambda = (SerializedLambda) method.invoke(this);
            // 获取函数方法名
            String methodName = serializedLambda.getImplMethodName();
            // 如果方法名以"get"开头，去掉前缀"get"
            if (methodName.startsWith("get")) {
                methodName = methodName.substring(3);
            } else if (methodName.startsWith("is")) {
                // 如果方法名以"is"开头，去掉前缀"is"
                methodName = methodName.substring(2);
            }
            // 首字母变小写，以符合Java字段名的常规命名规范
            return Func.firstToLowerCase(methodName);
        } catch (Exception e) {
            throw new EventBusException(e);
        }
    }
}
