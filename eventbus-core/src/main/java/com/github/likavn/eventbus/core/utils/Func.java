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
package com.github.likavn.eventbus.core.utils;

import com.github.likavn.eventbus.core.metadata.data.Request;
import com.github.likavn.eventbus.core.support.spi.IJson;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntConsumer;

/**
 * tool utils
 *
 * @author likavn
 * @date 2024/01/01
 */
@Slf4j
@UtilityClass
public final class Func {
    /**
     * agent class name pool
     */
    private static final List<String> PROXY_CLASS_NAMES = new ArrayList<>(4);

    private static final IJson JSON;

    static {
        // cglib
        PROXY_CLASS_NAMES.add("CGLIB$$");
        PROXY_CLASS_NAMES.add("cglib$$");
        // javassist
        PROXY_CLASS_NAMES.add("$$_JAVASSIST");
        PROXY_CLASS_NAMES.add("$$_javassist");

        // load JSON
        IJson js = null;
        ServiceLoader<IJson> serviceLoader = ServiceLoader.load(IJson.class);
        Integer minOrder = null;
        for (IJson t : serviceLoader) {
            if (!t.active()) {
                continue;
            }
            if (null == minOrder || t.getOrder() < minOrder) {
                minOrder = t.getOrder();
                js = t;
            }
        }
        JSON = js;
        Assert.notNull(JSON, "json serialization tool is required!");
    }

    /**
     * jsonStr to request bean
     *
     * @param js js
     * @return bean
     */
    @SuppressWarnings("all")
    public Request convertByJson(String js) {
        return parseObject(js, Request.class);
    }

    /**
     * jsonStr to request bean
     *
     * @param requestBytes bytes
     * @return bean
     */
    @SuppressWarnings("all")
    public Request convertByBytes(byte[] requestBytes) {
        return convertByJson(new String(requestBytes, StandardCharsets.UTF_8));
    }

    /**
     * @param body 数据对象
     * @param type 数据实体class
     * @return 转换对象
     */
    @SuppressWarnings("all")
    public <T> T parseObject(Object body, Type type) {
        if (body instanceof String) {
            String bodyStr = (String) body;
            if (!JSON.isJson(bodyStr) && isInterfaceImplemented((Class<?>) type, CharSequence.class)) {
                return (T) bodyStr;
            }
            return JSON.parseObject(body.toString(), type);
        }
        return JSON.parseObject(toJson(body), type);
    }

    /**
     * toJson
     *
     * @param value object
     * @return string
     */
    public String toJson(Object value) {
        return JSON.toJsonString(value);
    }

    public boolean isEmpty(Map<?, ?> map) {
        return null == map || map.size() == 0;
    }

    public boolean isEmpty(Object[] objs) {
        return null == objs || objs.length == 0;
    }

    public boolean isEmpty(Collection<?> list) {
        return null == list || list.isEmpty();
    }

    public boolean isEmpty(CharSequence str) {
        return str == null || str.length() == 0;
    }

    public boolean isEmpty(String str) {
        return str == null || str.trim().length() == 0;
    }

    /**
     * thread name pool
     */
    private final Map<String, String> theadNames = new ConcurrentHashMap<>();

    /**
     * process id
     */
    private final AtomicInteger processId = new AtomicInteger(1);

    /**
     * rename thread name
     *
     * @param name new name
     * @return old thread name
     */
    public String reThreadName(String name) {
        Thread thread = Thread.currentThread();
        String oldName = thread.getName();
        String newName = theadNames.computeIfAbsent(oldName, key -> name + processId.getAndAdd(1));
        thread.setName(newName);
        return oldName;
    }

    /**
     * 获取原始类型
     *
     * @param obj 传入对象
     * @return 原始类型
     */
    public Class<?> primitiveClass(Object obj) {
        return isProxy(obj.getClass()) ? obj.getClass().getSuperclass() : obj.getClass();
    }

    /**
     * 判断是否为代理对象
     *
     * @param clazz 传入 class 对象
     * @return 如果对象class是代理 class，返回 true
     */
    private boolean isProxy(Class<?> clazz) {
        for (String proxyClassName : PROXY_CLASS_NAMES) {
            if (clazz.getName().contains(proxyClassName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取主机名
     *
     * @return 主机名
     */
    public synchronized String getHostName() {
        return NetUtil.getHostName();
    }

    /**
     * 获取进程号
     *
     * @return 进程号
     */
    public String getPid() {
        String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
        return isEmpty(pid) ? "" : pid;
    }

    public String getTopic(String serviceId, String code) {
        Assert.notEmpty(serviceId, "serviceId can not be empty");
        if (Func.isEmpty(code)) {
            return serviceId;
        }
        return serviceId + "|" + code;
    }

    /**
     * 获取投递ID
     */
    public String getDeliverId(Class<?> clz, String methodName) {
        return String.format("%s#%s", clz.getName(), methodName);
    }

    /**
     * 轮询执行
     *
     * @param count    轮询次数
     * @param runnable 轮询任务
     */
    public void pollRun(int count, Runnable runnable) {
        for (int i = 0; i < count; i++) {
            runnable.run();
        }
    }

    /**
     * 轮询执行
     *
     * @param count    轮询次数
     * @param consumer 轮询任务
     */
    public void pollRun(int count, IntConsumer consumer) {
        for (int i = 0; i < count; i++) {
            consumer.accept(i);
        }
    }

    /**
     * 判断类是否实现了接口
     *
     * @param clazz          类
     * @param interfaceClass 接口
     * @return 是否实现了接口
     */
    public boolean isInterfaceImplemented(Class<?> clazz, Class<?> interfaceClass) {
        if (interfaceClass.isAssignableFrom(clazz)) {
            return true;
        }
        for (Class<?> inf : clazz.getInterfaces()) {
            if (isInterfaceImplemented(inf, interfaceClass)) {
                return true;
            }
        }
        return false;
    }
}
