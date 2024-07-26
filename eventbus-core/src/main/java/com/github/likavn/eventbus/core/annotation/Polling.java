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

import java.lang.annotation.*;

/**
 * 注解@Polling，用于标注在方法上，以控制消息订阅的轮询行为。
 * 轮询行为可以通过注解的属性count和interval进行配置。
 *
 * @author likavn
 * @date 2024/01/01
 **/
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Polling {

    /**
     * count属性定义了轮询的次数。默认值为-1，表示不进行轮询。
     * 如果设置了具体的轮询次数，则方法会在接收到消息后按照指定次数进行轮询。
     * 可通过{@link Polling.Keep#over()}编码方式调用，提前结束轮询任务。
     *
     * @return 轮询次数，默认为-1。
     */
    int count() default -1;

    /**
     * interval属性定义了轮询的时间间隔。时间间隔可以通过表达式(表达式只支持"+"、"*")进行配置
     * 表达式中可以使用两个变量：
     * count（当前轮询次数）和intervalTime（本次轮询与上次轮询的时间间隔，单位为秒）。
     * 这使得可以灵活地根据轮询次数和时间间隔来动态确定下一次轮询的时间。
     * 示例：
     * 1. interval=7，表示轮询间隔为7秒。
     * 2. interval=$count*$intervalTime，表示轮询间隔为当前轮询次数与上次轮询的时间间隔的乘积。
     *
     * @return 轮询时间间隔的表达式，默认为空字符串。
     */
    String interval() default "";

    /**
     * Keep类提供了一种机制来控制是否继续轮询。
     * 它通过ThreadLocal来存储一个布尔值，用于指示当前线程是否应该继续轮询。
     * 这个类提供了三个静态方法来操作这个状态：
     * isPoll()用于检查是否应该继续轮询，
     * over()用于标记轮询结束，
     * clear()用于清除轮询状态。
     *
     * @author likavn
     * @date 2024/01/01
     */
    @SuppressWarnings("all")
    class Keep {
        /**
         * ThreadLocal用于存储一个布尔值，用于指示当前线程是否应该继续轮询。
         * 默认值为true，表示应该继续轮询。
         */
        private static final ThreadLocal<Boolean> over = new ThreadLocal<>();

        /**
         * 检查是否应该继续轮询。
         *
         * @return 如果应该继续轮询则返回true，否则返回false。
         */
        public static boolean isOver() {
            return over.get() != null && over.get();
        }

        /**
         * 标记轮询结束。
         */
        public static void over() {
            over.set(Boolean.TRUE);
        }

        /**
         * 清除轮询状态。
         */
        public static void clear() {
            over.remove();
        }
    }
}

