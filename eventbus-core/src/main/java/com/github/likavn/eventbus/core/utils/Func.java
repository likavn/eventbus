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
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

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
     * @param body  数据对象
     * @param clazz 数据实体class
     * @return 转换对象
     */
    @SuppressWarnings("all")
    public <T> T parseObject(Object body, Class<T> clazz) {
        if (body instanceof String) {
            String bodyStr = (String) body;
            if (!JSON.isJson(bodyStr)) {
                return (T) bodyStr;
            }
            return JSON.parseObject(bodyStr, clazz);
        }
        return JSON.parseObject(toJson(body), clazz);
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
     * rename thread name
     *
     * @param name new name
     * @return old thread name
     */
    public String reThreadName(String name) {
        Thread thread = Thread.currentThread();
        String oldName = thread.getName();
        thread.setName(name + oldName.substring(oldName.lastIndexOf("-") + 1));
        return oldName;
    }

    @SuppressWarnings("all")
    public void resetPool(ThreadPoolExecutor poolExecutor) {
        if (null == poolExecutor) {
            return;
        }
        for (Future f : poolExecutor.getQueue().toArray(new Future[0])) {
            f.cancel(true);
        }
        poolExecutor.purge();
    }

    /**
     * 获取原始类型
     *
     * @param obj 传入对象
     * @return 原始类型
     */
    public static Class<?> primitiveClass(Object obj) {
        return isProxy(obj.getClass()) ? obj.getClass().getSuperclass() : obj.getClass();
    }

    /**
     * 判断是否为代理对象
     *
     * @param clazz 传入 class 对象
     * @return 如果对象class是代理 class，返回 true
     */
    private static boolean isProxy(Class<?> clazz) {
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
        return serviceId + "." + code;
    }

    /**
     * 获取投递ID
     */
    public String getDeliverId(Class<?> clz, String methodName) {
        return String.format("%s#%s", clz.getName(), methodName);
    }
}
