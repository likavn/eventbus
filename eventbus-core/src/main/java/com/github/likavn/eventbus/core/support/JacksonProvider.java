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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.likavn.eventbus.core.exception.EventBusException;
import com.github.likavn.eventbus.core.support.spi.IJson;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * jackson
 *
 * @author likavn
 * @date 2024/04/15
 * @since 2.2
 */
public class JacksonProvider implements IJson {

    @Override
    public String className() {
        return "com.fasterxml.jackson.databind.ObjectMapper";
    }

    @Override
    public String toJsonString(Object value) {
        try {
            return JacksonUtil.MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new EventBusException(e);
        }
    }

    @Override
    public <T> T parseObject(String text, Type type) {
        try {
            return JacksonUtil.MAPPER.readValue(text, JacksonUtil.MAPPER.constructType(type));
        } catch (IOException e) {
            throw new EventBusException(e);
        }
    }

    @Override
    public int getOrder() {
        return 3;
    }

    private static class JacksonUtil {
        private static final ObjectMapper MAPPER = new ObjectMapper();

        static {
            MAPPER.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
            MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        }
    }
}
