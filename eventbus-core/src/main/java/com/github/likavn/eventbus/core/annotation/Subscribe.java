package com.github.likavn.eventbus.core.annotation;

import com.github.likavn.eventbus.core.metadata.BusConfig;

import java.lang.annotation.*;

/**
 * 及时消息订阅注解
 *
 * @author likavn
 * @date 2024/01/01
 **/
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Subscribe {

    /**
     * 消息所属来源服务ID或服务名。默认订阅本服务配置的ID
     *
     * @see BusConfig#getServiceId()
     */
    String serviceId() default "";

    /**
     * 消息类型，用于区分不同的消息类型。
     */
    String[] codes();

    /**
     * 消息投递失败异常处理注解
     */
    Fail fail() default @Fail(callMethod = "");
}
