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

import com.github.likavn.eventbus.core.support.spi.IJson;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Type;
import java.util.ServiceLoader;

/**
 * json tool utils
 *
 * @author likavn
 * @date 2024/09/05
 */
@Slf4j
public class JsonSupportUtil {
    private static final IJson JSON;

    static {
        // load JSON
        IJson js = null;
        ServiceLoader<IJson> serviceLoader = ServiceLoader.load(IJson.class);
        Integer minOrder = null;
        for (IJson t : serviceLoader) {
            if (!t.active()) {
                continue;
            }
            if (null == minOrder || t.getOrder() < minOrder) {
                minOrder = t.getOrder();
                js = t;
            }
        }
        JSON = js;
        if (JSON == null) {
            log.error("json serialization tool is required!");
            System.exit(1);
        }
    }

    /**
     * toJson
     *
     * @param value object
     * @return string
     */
    public static String toJson(Object value) {
        return JSON.toJsonString(value);
    }

    /**
     * @param body 数据对象
     * @param type 数据实体class
     * @return 转换对象
     */
    @SuppressWarnings("all")
    public static <T> T parseObject(Object body, Type type) {
        if (body instanceof String) {
            String bodyStr = (String) body;
            if (!JSON.isJson(bodyStr) && Func.isInterfaceImpl((Class<?>) type, CharSequence.class)) {
                return (T) bodyStr;
            }
            return JSON.parseObject(body.toString(), type);
        }
        return JSON.parseObject(toJson(body), type);
    }
}
