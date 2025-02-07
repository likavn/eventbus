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

import lombok.experimental.UtilityClass;

import java.lang.annotation.*;

/**
 * 消息轮询行为注解
 * <p>
 * 定义了一个注解@Polling，用于标注在方法上以控制消息订阅的轮询行为。
 * 轮询可以通过注解的属性count设置轮巡次数，nextTime或interval进行配置轮巡时间。
 * <p>
 * 计算下一次轮巡时间，优先级从上至下：
 * 1.通过{@link Polling.Keep#over()}或{@link Keep#setNextTime(long)}设置；
 * 2.通过{@link Polling#nextTime()}设置；
 * 3.通过{@link Polling#interval()}设置；
 *
 * @author likavn
 * @date 2024/07/27
 * @since 2.3.2
 **/
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Polling {
    /**
     * 定义了轮询的次数。默认值为-1，表示不进行轮询。
     * 如果设置了具体的轮询次数，则方法会在接收到消息后按照指定次数进行轮询。
     * 可通过{@link Polling.Keep#over()}编码方式调用，提前结束轮询任务。
     *
     * @return 轮询次数。
     */
    int count();

    /**
     * 下次轮巡的间隔时间,单位：秒,默认-1为设置无效。
     */
    long nextTime() default -1L;

    /**
     * 定义了轮询的时间间隔，可通过表达式（支持“+”、“-”、“*”、“/”等运算符）配置。
     * 表达式中可以使用三个变量：count（当前轮询次数）、deliverCount（当前投递次数）和intervalTime（本次轮询与上次轮询的时间间隔，单位为秒）。
     * 这使得可以灵活地根据轮询次数和时间间隔来动态确定下一次轮询的时间。
     * 引用变量时使用"$"+变量名，例如"$count"。
     * 示例：
     * 1. interval=7，表示轮询间隔为7秒。
     * 2. interval=$count*$intervalTime，表示轮询间隔为当前轮询次数与上次轮询的时间间隔的乘积。
     *
     * @return 轮询时间间隔的表达式。
     */
    String interval() default "";

    /**
     * 编码方式设置下次轮巡时间
     *
     * @author likavn
     * @date 2024/07/27
     * @since 2.3.2
     */
    @UtilityClass
    class Keep {
        // 保存当前轮巡的下次轮巡时间
        private static final ThreadLocal<Long> NEXT_TIME = new ThreadLocal<>();

        /**
         * 获取当前线程的下一个时间戳
         *
         * @return 如果没有设置下一个时间戳，则返回0；否则返回设置的时间戳
         */
        public long nextTime() {
            return NEXT_TIME.get() == null ? 0 : NEXT_TIME.get();
        }

        /**
         * 设置当前线程的下一个时间戳
         *
         * @param nextTime 下一个时间戳
         */
        public void setNextTime(long nextTime) {
            NEXT_TIME.set(nextTime);
        }

        /**
         * 检查当前线程的时间戳是否已经结束
         *
         * @return 如果时间戳小于0，则表示已经结束，返回true；否则返回false
         */
        public boolean isOver() {
            return nextTime() < 0;
        }

        /**
         * 标记当前线程的时间戳为结束
         * 设置时间戳为-1，表示不再有下一个时间点
         */
        public void over() {
            NEXT_TIME.set(-1L);
        }

        /**
         * 清除当前线程的下一个时间戳
         * 使用ThreadLocal的remove方法来避免内存泄漏
         */
        public void clear() {
            NEXT_TIME.remove();
        }
    }
}
