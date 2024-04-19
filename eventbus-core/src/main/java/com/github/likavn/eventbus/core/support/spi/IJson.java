package com.github.likavn.eventbus.core.support.spi;

import com.github.likavn.eventbus.core.utils.Func;

/**
 * json工具SPI
 *
 * @author likavn
 * @date 2024/04/19
 */
public interface IJson {
    String PATTERN_JSON = "(\\{.*\\}|\\[.*\\])";

    /**
     * 判断字符串是否是json串
     *
     * @param val json
     * @return true是
     */
    default boolean isJson(String val) {
        if (Func.isEmpty(val)) {
            return false;
        }
        return val.matches(PATTERN_JSON);
    }

    /**
     * 用于判断是否可用
     *
     * @return true可用
     */
    boolean active();

    /**
     * to json string
     *
     * @param value v
     * @return json str
     */
    String toJsonString(Object value);

    /**
     * json 转对象
     *
     * @param jsonStr jsonStr
     * @param clazz   to bean class
     * @return bean
     */
    <T> T parseObject(String jsonStr, Class<T> clazz);

    /**
     * 当存在多个可用的json工具时，优先使用order最小的
     *
     * @return order 顺序
     */
    int getOrder();
}
