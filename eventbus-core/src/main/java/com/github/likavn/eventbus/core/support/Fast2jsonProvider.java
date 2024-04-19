package com.github.likavn.eventbus.core.support;

import com.alibaba.fastjson2.JSON;
import com.github.likavn.eventbus.core.support.spi.IJson;

/**
 * fastjson2
 *
 * @author likavn
 */
public class Fast2jsonProvider implements IJson {
    @Override
    public boolean active() {
        try {
            Class.forName("com.alibaba.fastjson2.JSON");
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
        return 1;
    }
}
