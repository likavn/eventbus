package com.github.likavn.eventbus.core.support;

import com.alibaba.fastjson.JSON;
import com.github.likavn.eventbus.core.support.spi.IJson;

/**
 * fastjson
 *
 * @author likavn
 */
public class FastjsonProvider implements IJson {
    @Override
    public boolean active() {
        try {
            Class.forName("com.alibaba.fastjson.JSON");
        } catch (ClassNotFoundException e) {
            return false;
        }
        return true;
    }

    @Override
    public String toJsonString(Object value) {
        return JSON.toJSONString(value);
    }

    @Override
    public <T> T parseObject(String text, Class<T> clazz) {
        return JSON.parseObject(text, clazz);
    }

    @Override
    public int getOrder() {
        return 2;
    }
}
