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

import com.github.likavn.eventbus.core.exception.EventBusException;
import com.github.likavn.eventbus.core.metadata.data.Request;
import com.github.likavn.eventbus.core.support.spi.IJson;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
    /**
     * 用于缓存本地非回环IPv4地址，避免重复枚举网络接口
     */
    private InetAddress cachedAddress;

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
        if (JSON == null) {
            log.error("json serialization tool is required!");
            System.exit(1);
        }
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
     * toJson
     *
     * @param value object
     * @return string
     */
    @SuppressWarnings("all")
    public String toJson(Object value) {
        return JSON.toJsonString(value);
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
            if (!JSON.isJson(bodyStr) && Func.isInterfaceImpl((Class<?>) type, CharSequence.class)) {
                return (T) bodyStr;
            }
            return JSON.parseObject(body.toString(), type);
        }
        return JSON.parseObject(toJson(body), type);
    }

    /**
     * 获取本地非回环IPv4地址
     *
     * @return 本地非回环IPv4地址，如果没有找到则抛出EventBusException
     */
    public InetAddress getFirstNonLoopBackIPv4Address() {
        // 检查缓存
        if (cachedAddress != null) {
            return cachedAddress;
        }
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (address instanceof Inet4Address && !address.isLoopbackAddress()) {
                        // 找到合适的地址，缓存并返回
                        cachedAddress = address;
                        return address;
                    }
                }
            }
        } catch (SocketException e) {
            // 抛出更具体的异常，并包含原始异常信息
            throw new EventBusException("获取本地非回环IPv4地址失败", e);
        }

        // 如果没有找到合适的地址，抛出异常
        throw new EventBusException("获取本地非回环IPv4地址失败");
    }

    /**
     * 获取本地hostName
     *
     * @return 本地hostName
     */
    public String getHostAddr() {
        return getFirstNonLoopBackIPv4Address().getHostAddress();
    }

    public boolean isEmpty(Map<?, ?> map) {
        return null == map || map.isEmpty();
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
        return str == null || str.trim().isEmpty();
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
    public Class<?> originalClass(Object obj) {
        if (null == obj) {
            return null;
        }
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
     * 获取主机地址
     *
     * @return 主机地址
     */
    public String getHostAddress() {
        return Func.getHostAddr();
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

    /**
     * 拼接主题
     *
     * @param names 主题名称
     * @return 主题字符串
     */
    public String topic(String... names) {
        return String.join("|", names);
    }

    /**
     * 消息主题
     * <p>
     * 根据服务ID和消息编码生成主题
     */
    public String getTopic(String serviceId, String code) {
        return topic(serviceId, code);
    }

    /**
     * 投递主题
     * <p>
     * 根据服务ID和投递ID生成主题字符串
     */
    public String getDeliverTopic(String serviceId, String deliverId) {
        return topic(serviceId, deliverId);
    }

    /**
     * 全名称主题
     * <p>
     * 根据服务ID和消息编码和投递ID生成主题字符串
     */
    public String getFullTopic(Request<?> request) {
        return topic(request.getServiceId(), request.getCode(), request.getDeliverId());
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
     * 判断类是否实现了接口
     *
     * @param clazz          类
     * @param interfaceClass 接口
     * @return 是否实现了接口
     */
    public boolean isInterfaceImpl(Class<?> clazz, Class<?> interfaceClass) {
        if (interfaceClass.isAssignableFrom(clazz)) {
            return true;
        }
        for (Class<?> inf : clazz.getInterfaces()) {
            if (isInterfaceImpl(inf, interfaceClass)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 正则表达式，只允许包含数字和字母的字符串
     */
    private static final Pattern VALID_NAME_PATTERN = Pattern.compile("[A-Za-z0-9_\\-]+");

    /**
     * 验证名称是否有效
     *
     * @param name 待验证的名称
     * @return 如果名称符合预定义的有效名称模式，则返回true；否则返回false
     */
    public boolean valid(String name) {
        return VALID_NAME_PATTERN.matcher(name).matches();
    }

    /**
     * 通过反射设置指定对象的指定字段的值
     *
     * @param bean      要设置值的对象实例
     * @param fieldName 要设置值的字段名称
     * @param v         要设置的值
     */
    @SuppressWarnings("all")
    public void setBean(Object bean, String fieldName, Object v) {
        try {
            // 获取指定对象类中指定名称的字段
            Field field = bean.getClass().getDeclaredField(fieldName);
            // 设置字段可访问，绕过访问修饰符限制
            field.setAccessible(true);
            // 设置字段的值
            field.set(bean, v);
        } catch (Exception e) {
            // 如果发生异常，抛出自定义的EventBusException
            throw new EventBusException(e);
        }
    }

    /**
     * 首字母转换小写
     *
     * @param str 需要转换的字符串
     * @return 转换好的字符串
     */
    public String firstToLowerCase(final String str) {
        if (isEmpty(str)) {
            return "";
        }
        return str.substring(0, 1).toLowerCase() + str.substring(1);
    }

    /**
     * 根据指定的函数，去除列表中的重复元素
     *
     * @param list 要去重的列表
     * @param fn   用于将列表元素转换为其他类型的函数，以便确定重复元素
     * @param <T>  列表元素的类型
     * @param <R>  转换后用作Map键的类型
     * @return 去重后的列表
     */
    public <T, R> List<T> distinct(List<T> list, Function<T, R> fn) {
        return new ArrayList<>(list.stream().collect(Collectors.toMap(fn, Function.identity(), (v1, v2) -> v1, LinkedHashMap::new)).values());
    }
}
