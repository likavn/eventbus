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
package com.github.likavn.eventbus.provider.rocket.constant;

/**
 * rocketmq常量
 *
 * @author likavn
 * @date 2024/01/01
 * @since 2.2
 */
public class RocketConstant {
    private RocketConstant() {
    }

    /**
     * 前缀
     */
    private static final String SUFFIX = "eventbus_";

    /**
     * 及时消息队列key
     * 参数：
     * <p>
     * 1.服务ID|消息编码;
     */
    public static final String TIMELY_QUEUE = SUFFIX + "timely_queue_%s";

    /**
     * 及时消息重试队列key
     * 参数：
     * <p>
     * 1.服务ID|消息编码|消息监听器全类名;
     */
    public static final String TIMELY_RETRY_QUEUE = SUFFIX + "timely_retry_queue_%s";

    /**
     * 延时消息队列名key
     * 参数：
     * <p>
     * 1.服务ID|消息编码;
     */
    public static final String DELAY_QUEUE = SUFFIX + "delay_queue_%s";

    /**
     * 延时消息重试队列名key
     * 参数：
     * <p>
     * 1.服务ID|消息编码|消息监听器全类名;
     */
    public static final String DELAY_RETRY_QUEUE = SUFFIX + "delay_retry_queue_%s";
}
