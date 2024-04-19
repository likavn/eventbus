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

/**
 * jackson
 *
 * @author likavn
 */
public class JacksonProvider implements IJson {

    @Override
    public boolean active() {
        try {
            Class.forName("com.fasterxml.jackson.databind.ObjectMapper");
        } catch (ClassNotFoundException e) {
            return false;
        }
        return true;
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
    public <T> T parseObject(String text, Class<T> clazz) {
        try {
            return JacksonUtil.MAPPER.readValue(text, clazz);
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
