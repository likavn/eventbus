package com.github.likavn.notify.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;
import org.springframework.lang.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SpringUtil
 *
 * @author likavn
 * @since 2023/01/01
 **/
public class SpringUtil{

    private static final Map<Class<?>, Object> CACHE_BEANS = new ConcurrentHashMap<>();
    /**
     * 应用ID
     */
    private static String serviceId;

    /**
     * ctx
     */
    private static ApplicationContext context;
    private static Environment environment;

    @SuppressWarnings("all")
    public static void  setApplicationContext(@Nullable ApplicationContext context, Environment environment) throws BeansException {
        SpringUtil.context = context;
        SpringUtil.environment = environment;
    }

    @SuppressWarnings("all")
    public static <T> T getBean(Class<T> requiredType) {
        return (T) CACHE_BEANS.computeIfAbsent(requiredType, cl -> context.getBean(requiredType));
    }

    /**
     * 获取应用名称
     */
    public static String getServiceId() {
        if (Objects.nonNull(serviceId)) {
            return serviceId;
        }
        serviceId = environment.getProperty("spring.application.name");
        if (null == serviceId || serviceId.isEmpty()) {
            serviceId = System.getProperties().getProperty("sun.java.command");
        }
        return serviceId;
    }

}
