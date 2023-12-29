package com.github.likavn.eventbus.core.utils;


import lombok.experimental.UtilityClass;

import java.util.Collection;
import java.util.Map;

/**
 * Assert
 *
 * @author likavn
 * @date 2024/01/01
 **/
@UtilityClass
public class Assert {
    /**
     * 判断条件是否为真，如果不是真则抛出非法参数异常
     *
     * @param expression 判断条件
     * @param message    异常信息
     */
    public void isTrue(boolean expression, String message) {
        if (!expression) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 判断对象是否为null
     *
     * @param object  待判断对象
     * @param message 异常信息
     * @throws IllegalArgumentException 如果对象不为null
     */
    public void isNull(Object object, String message) {
        if (object != null) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 检查给定的对象是否不为空
     * 如果对象为空，则抛出 IllegalArgumentException 异常
     *
     * @param object  需要检查的对象
     * @param message 异常信息
     * @throws IllegalArgumentException 如果对象为空
     */
    public void notNull(Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 检查数组不为空
     *
     * @param array   数组
     * @param message 消息
     * @throws IllegalArgumentException 如果数组为空或数组中存在空元素
     */
    public void notEmpty(Object[] array, String message) {
        if (Func.isEmpty(array)) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 检查集合不能为空
     *
     * @param collection 集合
     * @param message    异常信息
     * @throws IllegalArgumentException 如果集合为空，抛出异常
     */
    public void notEmpty(Collection<?> collection, String message) {
        if (Func.isEmpty(collection)) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 检查Map不为空
     *
     * @param map     Map对象
     * @param message 错误信息
     * @throws IllegalArgumentException 如果Map为空，抛出此异常
     */
    public void notEmpty(Map<?, ?> map, String message) {
        if (Func.isEmpty(map)) {
            throw new IllegalArgumentException(message);
        }
    }
}
