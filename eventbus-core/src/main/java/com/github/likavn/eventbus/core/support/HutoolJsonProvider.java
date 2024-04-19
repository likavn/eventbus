package com.github.likavn.eventbus.core.support;

import cn.hutool.json.JSONUtil;
import com.github.likavn.eventbus.core.support.spi.IJson;

/**
 * hutool json
 * <a href="https://hutool.cn/docs/#/json/JSONUtil">hutool json</a>
 *
 * @author likavn
 */
public class HutoolJsonProvider implements IJson {
    @Override
    public boolean active() {
        try {
            Class.forName("cn.hutool.json.JSONUtil");
        } catch (ClassNotFoundException e) {
            return false;
        }
        return true;
    }

    @Override
    public String toJsonString(Object value) {
        return JSONUtil.toJsonStr(value);
    }

    @Override
    public <T> T parseObject(String text, Class<T> clazz) {
        return JSONUtil.toBean(text, clazz);
    }

    @Override
    public int getOrder() {
        return 5;
    }
}
