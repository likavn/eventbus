package com.github.likavn.eventbus.core.annotation;

import java.lang.annotation.*;

/**
 * tips: 消息投递失败异常处理注解，
 * 使用在消息重试投递最终失败时进行回调。必须和订阅器在同一个类中
 *
 * @author likavn
 * @since 2023/05/15
 */
@Documented
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Fail {
    /**
     * 失败回调方法，回调方法必须和订阅器处理器在同一个类中
     * <p>回调方法可选参数（无序）：</p>
     * 1：消息体{@link com.github.likavn.eventbus.core.metadata.data.Message}
     * 1：重复投递失败异常，为原始异常{@link java.lang.Exception}
     */
    String callMethod();

    /**
     * 消息投递失败时，一定时间内再次进行投递的次数，默认为3次
     */
    int retry() default 3;

    /**
     * 投递失败时，下次下次投递触发的间隔时间,单位：秒
     */
    long nextTime() default 10L;
}
