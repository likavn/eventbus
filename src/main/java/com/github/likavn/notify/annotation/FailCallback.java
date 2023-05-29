package com.github.likavn.notify.annotation;

import com.github.likavn.notify.domain.Message;

import java.lang.annotation.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 消息投递失败异常处理注解
 * 消息重试投递失败时回调
 * <p>必须和消费者在同一个类中</p>
 * <p>可选参数：</p>
 * 1：消息体{@link com.github.likavn.notify.domain.Message}
 * 1：重复投递失败异常{@link Exception}
 *
 * @author likavn
 * @since 2023/05/15
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface FailCallback {

    /**
     * 回调
     */
    @SuppressWarnings("all")
    public static class Error {
        private static final Map<Class, Method> failCallbackMethodMap = new ConcurrentHashMap<>();

        private static final Set<Class> nullSet = new CopyOnWriteArraySet<>();

        /**
         * 投递失败回调
         *
         * @param _this     消费者对象
         * @param message   msg
         * @param exception e
         */
        public static void onFail(Object _this, Message message, Throwable childEx)
                throws InvocationTargetException, IllegalAccessException {
            if (null == _this) {
                return;
            }
            Method callback = getFailCallback(_this);
            if (null == callback) {
                return;
            }
            Class<?>[] parameterTypes = callback.getParameterTypes();
            Object[] args = new Object[parameterTypes.length];
            int index = 0;
            for (Class<?> parameterType : parameterTypes) {
                if (isInterface(message.getClass(), parameterType)) {
                    args[index] = message;
                } else if (isInherited(childEx.getClass(), parameterType)) {
                    args[index] = childEx;
                }
                index++;
            }

            callback.invoke(_this, args);
        }

        /**
         * 获取失败回调方法
         *
         * @param _this
         * @return
         */
        private static Method getFailCallback(Object _this) {
            Class<?> keyClass = _this.getClass();

            // 处理器不存在失败回调函数
            if (nullSet.contains(_this)) {
                return null;
            }
            Method method = failCallbackMethodMap.get(keyClass);
            if (null != method) {
                return method;
            }

            for (Method md : keyClass.getMethods()) {
                FailCallback annotation = md.getAnnotation(FailCallback.class);
                if (null != annotation) {
                    method = md;
                    break;
                }
            }
            if (null == method) {
                nullSet.add(keyClass);
            } else {
                failCallbackMethodMap.put(keyClass, method);
            }
            return method;
        }

        /**
         * 判断类是否存在继承关系
         *
         * @param child
         * @param parent
         * @return
         */
        public static boolean isInherited(Class child, Class parent) {
            for (Class type = child; type != null; type = type.getSuperclass()) {
                if (type.equals(parent)) {
                    return true;
                }
            }
            return false;
        }

        /**
         * 判断类是否实现接口
         *
         * @param child
         * @param interfaceName
         * @return
         */
        public static boolean isInterface(Class child, Class interfaceName) {
            for (Class intf : child.getInterfaces()) {
                if (intf.equals(interfaceName)) {
                    return true;
                }
            }
            return false;
        }
    }

}
