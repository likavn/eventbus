package com.github.likavn.eventbus.core.utils;

import com.alibaba.fastjson.JSONValidator;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.github.likavn.eventbus.core.exception.EventBusException;
import com.github.likavn.eventbus.core.metadata.data.Request;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 工具类
 *
 * @author likavn
 * @date 2024/01/01
 */
@Slf4j
@UtilityClass
public class Func {
    private static volatile String HOST_NAME = null;

    /**
     * toJson
     *
     * @param value object
     * @return string
     */
    public String toJson(Object value) {
        return JSON.toJSONString(value);
    }

    /**
     * json数据转换为实体数据类型
     *
     * @param js js
     * @return bean
     */
    @SuppressWarnings("all")
    public Request convertByJson(String js) {
        return JSON.parseObject(js, Request.class, JSONReader.Feature.SupportClassForName);
    }

    /**
     * 二进制数据转换为实体数据类型
     *
     * @param requestBytes bytes
     * @return bean
     */
    @SuppressWarnings("all")
    public Request convertByBytes(byte[] requestBytes) {
        return JSON.parseObject(requestBytes, Request.class, JSONReader.Feature.SupportClassForName);
    }

    /**
     * @param body  数据对象
     * @param clazz 数据实体class
     * @return 转换对象
     */
    @SuppressWarnings("all")
    public <T> T parseObject(Object body, Class<T> clazz) {
        if (body instanceof String) {
            String bodyStr = body.toString();
            if (!JSONValidator.from(bodyStr).validate()) {
                return (T) bodyStr;
            }
            return JSON.parseObject(bodyStr, clazz, JSONReader.Feature.SupportClassForName);
        }
        return JSON.parseObject(toJson(body), clazz, JSONReader.Feature.SupportClassForName);
    }

    public boolean isEmpty(Map<?, ?> map) {
        return null == map || map.size() <= 0;
    }

    public boolean isEmpty(Object[] objs) {
        return null == objs || objs.length <= 0;
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
     * 线程重新命名
     *
     * @param name 新名称
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
        for (Future f : poolExecutor.getQueue().toArray(new Future[0])) {
            f.cancel(true);
        }
        poolExecutor.purge();
    }

    /**
     * 获取主机名
     *
     * @return 主机名
     */
    public synchronized String getHostName() {
        if (null == HOST_NAME) {
            try {
                HOST_NAME = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                throw new EventBusException(e);
            }
        }
        return HOST_NAME;
    }

    public String getTopic(String serviceId, String code) {
        Assert.notEmpty(serviceId, "serviceId can not be empty");
        if (Func.isEmpty(code)) {
            return serviceId;
        }
        return serviceId + "." + code;
    }
}
