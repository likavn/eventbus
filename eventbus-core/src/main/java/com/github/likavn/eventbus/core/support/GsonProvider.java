package com.github.likavn.eventbus.core.support;

import com.github.likavn.eventbus.core.support.spi.IJson;
import com.google.gson.Gson;

/**
 * gson
 *
 * @author likavn
 */
public class GsonProvider implements IJson {

    @Override
    public boolean active() {
        try {
            Class.forName("com.google.gson.Gson");
        } catch (ClassNotFoundException e) {
            return false;
        }
        return true;
    }

    @Override
    public String toJsonString(Object value) {
        return GsonUtil.GSON.toJson(value);
    }

    @Override
    public <T> T parseObject(String text, Class<T> clazz) {
        return GsonUtil.GSON.fromJson(text, clazz);
    }

    @Override
    public int getOrder() {
        return 4;
    }

    private static class GsonUtil {
        public static final Gson GSON = new Gson();
    }
}
