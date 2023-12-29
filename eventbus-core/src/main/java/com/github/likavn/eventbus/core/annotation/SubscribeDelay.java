package com.github.likavn.eventbus.core.annotation;

import java.lang.annotation.*;

/**
 * 延时消息订阅注解
 *
 * @author likavn
 * @date 2023/6/15
 **/
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SubscribeDelay {
    /**
     * 消息类型，用于区分不同的消息类型。
     */
    String[] codes();

    /**
     * 消息投递失败异常处理注解
     */
    Fail fail() default @Fail(callMethod = "");
}
