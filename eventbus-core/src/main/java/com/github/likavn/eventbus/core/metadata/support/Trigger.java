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
import lombok.Data;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

/**
 * 触发器实体
 *
 * @author likavn
 * @date 2024/01/01
 **/
@Data
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
     * 接收数据所在参数列表位置
     */
    private Integer messageIndex;

    /**
     * 接收数据所在参数数据类型
     */
    private Class<?> messageClazz;

    /**
     * 异常所在参数列表位置
     */
    private Integer throwableIndex;

    /**
     * 参数数量
     */
    private int paramsCount;

    protected Trigger(Object invokeBean, Method method) {
        this.invokeBean = invokeBean;
        this.method = method;
        // 构建参数
        buildParams(method);
    }

    /**
     * 构建触发器
     *
     * @param invokeBean 调用对象
     * @param method     方法
     * @return 触发器
     */
    public static Trigger of(Object invokeBean, Method method) {
        return new Trigger(invokeBean, method);
    }

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
        Request request = (Request) message;
        Object oldBody = request.getBody();
        try {
            Object[] args = new Object[this.paramsCount];
            if (this.paramsCount > 0) {
                if (this.messageIndex >= 0) {
                    args[this.messageIndex] = message;
                    if (null != messageClazz) {
                        request.setBody(Func.parseObject(message.getBody(), messageClazz));
                    }
                }
                if (this.throwableIndex >= 0) {
                    args[this.throwableIndex] = throwable;
                }
            }
            method.invoke(invokeBean, args);
        } catch (Exception e) {
            throw new EventBusException(e);
        } finally {
            request.setBody(oldBody);
        }
    }

    /**
     * 构建参数
     */
    private void buildParams(Method method) {
        try {
            int modifiers = method.getModifiers();
            Assert.isTrue(Modifier.isPublic(modifiers),
                    String.format("Method %s of %s must be public", method.getName(), method.getDeclaringClass().getName()));
            Type[] parameterTypes = method.getGenericParameterTypes();
            this.paramsCount = parameterTypes.length;
            this.messageIndex = -1;
            this.throwableIndex = -1;
            for (int index = 0; index < parameterTypes.length; index++) {
                String typeName = parameterTypes[index].getTypeName();
                // 接收消息
                if (typeName.contains(Message.class.getName())) {
                    messageIndex = index;
                    int bodyIndex = typeName.indexOf('<');
                    if (bodyIndex > 0) {
                        String bodyClsName = typeName.substring(bodyIndex + 1, typeName.length() - 1);
                        this.messageClazz = Class.forName(bodyClsName);
                    }
                }
                // 接收异常
                else if (typeName.contains(Throwable.class.getName())) {
                    throwableIndex = index;
                }
            }
        } catch (Exception e) {
            throw new EventBusException(e);
        }
    }

}
