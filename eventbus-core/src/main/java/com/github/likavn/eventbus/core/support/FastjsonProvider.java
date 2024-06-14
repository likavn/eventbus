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
package com.github.likavn.eventbus.core.support;

import com.alibaba.fastjson.JSON;
import com.github.likavn.eventbus.core.support.spi.IJson;

import java.lang.reflect.Type;

/**
 * fastjson
 *
 * @author likavn
 * @date 2024/04/15
 * @since 2.2
 */
public class FastjsonProvider implements IJson {
    @Override
    public String className() {
        return "com.alibaba.fastjson.JSON";
    }

    @Override
    public String toJsonString(Object value) {
        return JSON.toJSONString(value);
    }

    @Override
    public <T> T parseObject(String text, Type type) {
        return JSON.parseObject(text, type);
    }

    @Override
    public int getOrder() {
        return 2;
    }
}
