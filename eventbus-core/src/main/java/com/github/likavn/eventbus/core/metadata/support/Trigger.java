package com.github.likavn.eventbus.core.metadata.support;

import com.github.likavn.eventbus.core.metadata.data.Message;
import com.github.likavn.eventbus.core.utils.Func;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.Method;

/**
 * 触发器实体
 *
 * @author likavn
 * @date 2024/01/01
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Trigger {
    /**
     * 调用对象
     */
    private Object invokeBean;

    /**
     * 方法
     */
    private Method method;

    /**
     * 方法形参类型列表
     */
    private Class<?>[] argTypes;

    /**
     * 投递ID
     */
    public String getDeliverId() {
        return String.format("%s#%s", invokeBean.getClass().getName(), method.getName());
    }

    /**
     * 触发调用
     *
     * @param message 消息
     */
    public void invoke(Message message) {
        invoke(message);
    }

    /**
     * 触发调用
     *
     * @param message   消息
     * @param exception 异常
     */
    public void invoke(Message message, Exception exception) {
        if (Func.isEmpty(argTypes)) {
            return;
        }
        Object[] args = new Object[argTypes.length];
        int index = 0;
        for (Class<?> type : argTypes) {
            if (Func.isInterface(message.getClass(), type)) {
                args[index] = message;
            } else if (Func.isInherited(exception.getClass(), type)) {
                args[index] = exception;
            }
            index++;
        }

        try {
            method.invoke(invokeBean, args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
