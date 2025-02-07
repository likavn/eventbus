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
package com.github.likavn.eventbus.core.annotation;

import com.github.likavn.eventbus.core.metadata.BusConfig;
import lombok.experimental.UtilityClass;

import java.lang.annotation.*;

/**
 * 投递重试注解
 * <p>
 * 定义了一个注解@FailRetry，用于标注在方法上以控制消息订阅的轮询行为。
 * 轮询可以通过注解的属性count设置重试次数，nextTime或interval进行配置下次消息的重试时间。
 * <p>
 * 获取有效的（间隔时间>0）下一次重试时间，优先级从上至下：
 * 1.通过{@link FailRetry.Keep#setNextTime(long)}设置下次重试时间；
 * 2.通过{@link FailRetry#nextTime()}设置下次重试时间；
 * 3.通过{@link FailRetry#interval()}设置下次重试时间的表达式；
 * 4.根据全局配置{@link BusConfig.Fail#getNextTime()} 设置下次重试时间。
 *
 * @author likavn
 * @date 2024/01/01
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface FailRetry {
    /**
     * 消息投递失败时，一定时间内再次进行投递的次数
     * <code>count < 0</code> 时根据全局配置{@link BusConfig.Fail#getRetryCount()} 默认为3次
     */
    int count() default -1;

    /**
     * 投递失败时，下次投递触发的间隔时间,单位：秒
     */
    long nextTime() default -1L;

    /**
     * 定义了投递失败时，下次重试消息的时间间隔，可通过表达式（支持“+”、“-”、“*”、“/”等运算符）配置。
     * 表达式中可以使用三个变量：count（当前失败重试次数）、deliverCount（当前投递次数）和intervalTime（本次重试与上次投递的时间间隔，单位：秒）。
     * 这使得可以灵活地根据重试次数和时间间隔来动态确定下一次重试的时间。
     * 引用变量时使用"$"+变量名，例如"$count"。
     * 示例：
     * 1. interval=7，表示重试间隔为7秒。
     * 2. interval=$count*$intervalTime，表示重试间隔为当前重试次数与上次投递的时间间隔的乘积。
     *
     * @return 重试时间间隔的表达式。
     */
    String interval() default "";

    /**
     * 编码方式设置失败重试时间
     *
     * @author likavn
     * @date 2025/02/07
     * @since 2.5.0
     */
    @UtilityClass
    class Keep {
        // 当前任务下次重试时间
        private static final ThreadLocal<Long> NEXT_TIME = new ThreadLocal<>();

        /**
         * 获取当前消息下次重试时间
         *
         * @return 下次重试时间，单位：秒
         */
        public long nextTime() {
            return NEXT_TIME.get() == null ? 0 : NEXT_TIME.get();
        }

        /**
         * 设置当前消息下次重试时间
         *
         * @param nextTime 下次重试时间，单位：秒
         */
        public void setNextTime(long nextTime) {
            NEXT_TIME.set(nextTime);
        }

        /**
         * 清除
         */
        public void clear() {
            NEXT_TIME.remove();
        }
    }
}
