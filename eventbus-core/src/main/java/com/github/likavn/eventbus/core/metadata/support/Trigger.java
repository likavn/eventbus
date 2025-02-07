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
package com.github.likavn.eventbus.core.metadata.support;

import com.github.likavn.eventbus.core.exception.EventBusException;
import com.github.likavn.eventbus.core.metadata.data.Message;
import com.github.likavn.eventbus.core.metadata.data.Request;
import com.github.likavn.eventbus.core.utils.Assert;
import com.github.likavn.eventbus.core.utils.Func;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.*;

/**
 * 触发器实体
 *
 * @author likavn
 * @date 2024/01/01
 **/
@Slf4j
public class Trigger {
    /**
     * 调用对象
     */
    @Getter
    private final Object invokeBean;

    /**
     * 调用对象class
     */
    @Getter
    private final Class<?> primitiveClass;

    /**
     * 方法
     */
    @Getter
    private final Method method;

    /**
     * 接收数据所在参数列表位置
     */
    private Integer messageDataIndex;

    /**
     * 接收数据所在参数数据类型
     */
    private Type messageDataType;

    /**
     * 异常所在参数列表位置
     */
    private Integer throwableIndex;

    /**
     * 参数数量
     */
    private int paramsCount;

    /**
     * 投递ID
     */
    @Getter
    private final String deliverId;

    protected Trigger(Object invokeBean, Method method) {
        this.invokeBean = invokeBean;
        this.method = method;
        this.primitiveClass = Func.originalClass(invokeBean);
        this.deliverId = null == primitiveClass ? null : primitiveClass.getName();
        // 构建参数
        buildParams(invokeBean, method);
    }

    /**
     * 构建触发器
     *
     * @param invokeBean 调用对象
     * @param method     方法
     * @return 触发器
     */
    public static Trigger of(Object invokeBean, Method method) {
        if (null == method) {
            return null;
        }
        return new Trigger(invokeBean, method);
    }

    /**
     * 触发调用
     *
     * @param message 消息
     */
    @SuppressWarnings("all")
    public void invoke(Message message) {
        invoke(message, null);
    }

    /**
     * 触发调用
     *
     * @param message   消息
     * @param throwable 异常
     */
    @SuppressWarnings("all")
    public void invoke(Message message, Throwable throwable) {
        if (null == method) {
            return;
        }
        Request request = (Request) message;
        Object oldBody = request.getBody();
        try {
            Object[] args = new Object[this.paramsCount];
            if (this.paramsCount > 0) {
                if (this.messageDataIndex >= 0) {
                    request.setBody(Func.parseObject(message.getBody(), messageDataType));
                    args[this.messageDataIndex] = message;
                }
                if (this.throwableIndex >= 0) {
                    args[this.throwableIndex] = throwable;
                }
            }
            method.invoke(invokeBean, args);
        } catch (Exception ex) {
            throwable = ex;
            if (ex instanceof InvocationTargetException) {
                throwable = ex.getCause();
                // 接收消息时抛出异常
                log.error("trigger invoke error", throwable);
            }
            throw new EventBusException(throwable);
        } finally {
            request.setBody(oldBody);
        }
    }

    /**
     * 构建参数
     */
    private void buildParams(Object invokeBean, Method method) {
        if (null == invokeBean || null == method) {
            return;
        }
        int modifiers = method.getModifiers();
        try {
            Assert.isTrue(Modifier.isPublic(modifiers),
                    String.format("Method %s of %s must be public", method.getName(), method.getDeclaringClass().getName()));
            method = primitiveClass.getMethod(method.getName(), method.getParameterTypes());
            Type[] parameterTypes = method.getGenericParameterTypes();
            this.paramsCount = parameterTypes.length;
            this.messageDataIndex = -1;
            this.throwableIndex = -1;
            for (int index = 0; index < parameterTypes.length; index++) {
                String typeName = parameterTypes[index].getTypeName();
                // 接收消息
                if (typeName.contains(Message.class.getName())) {
                    messageDataIndex = index;
                    messageDataType = ((ParameterizedType) parameterTypes[index]).getActualTypeArguments()[0];
                }
                // 接收异常
                else if (typeName.contains(Throwable.class.getName())) {
                    throwableIndex = index;
                }
            }
        } catch (Exception e) {
            log.error("Trigger.buildParams", e);
            System.exit(1);
        }
    }

}
