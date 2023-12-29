package com.github.likavn.eventbus.core.annotation;

import java.lang.annotation.*;

/**
 * 及时消息订阅注解
 *
 * @author likavn
 * @date 2023/6/15
 **/
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Subscribe {

    /**
     * 消息所属来源服务ID或服务名。默认订阅本服务消息
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
