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
package com.github.likavn.eventbus.provider.rabbit.constant;

/**
 * rabbitmq常量
 *
 * @author likavn
 * @date 2024/01/01
 */
public class RabbitConstant {
    private RabbitConstant() {
    }

    /**
     * 前缀
     */
    private static final String SUFFIX = "ebus.";

    /**
     * 及时消息交换机
     * 参数：
     * <p>
     * 1.服务serviceId;
     */
    public static final String TIMELY_EXCHANGE = SUFFIX + "t.exchange.%s";

    /**
     * 及时消息路由key；
     * 参数：
     * <p>
     * 1.服务serviceId|消息编码;
     */
    public static final String TIMELY_ROUTING_KEY = SUFFIX + "t.routingKey.%s";

    /**
     * 及时消息队列key；
     * 参数：
     * <p>
     * 1.服务serviceId|消息编码|监听器全类名;
     */
    public static final String TIMELY_QUEUE = SUFFIX + "t.queue.%s";

    /**
     * 及时消息重试路由key；
     * 参数：
     * <p>
     * 1.服务serviceId|消息编码|监听器全类名;
     */
    public static final String TIMELY_RETRY_ROUTING_KEY = SUFFIX + "t.retry.routingKey.%s";

    /**
     * 及时消息重试队列key；
     * 参数：
     * <p>
     * 1.服务serviceId|消息编码|监听器全类名;
     */
    public static final String TIMELY_RETRY_QUEUE = SUFFIX + "t.retry.queue.%s";

    /**
     * 延时队列交换机
     * 参数：
     * <p>
     * 1.服务serviceId;
     */
    public static final String DELAY_EXCHANGE = SUFFIX + "d.exchange.%s";

    /**
     * 延时队列路由key
     * 参数：
     * <p>
     * 1.服务serviceId|消息编码
     */
    public static final String DELAY_ROUTING_KEY = SUFFIX + "d.routingKey.%s";

    /**
     * 延时队列名
     * 参数：
     * <p>
     * 1.服务serviceId|消息编码|监听器全类名;
     */
    public static final String DELAY_QUEUE = SUFFIX + "d.queue.%s";

    /**
     * 延时队列路由key,异常重试路由
     * 参数：
     * <p>
     * 1.服务serviceId|消息编码|监听器全类名;
     */
    public static final String DELAY_RETRY_ROUTING_KEY = SUFFIX + "d.retry.routingKey.%s";

    /**
     * 延时消息重试队列key
     * 参数：
     * <p>
     * 1.服务serviceId|消息编码|监听器全类名;
     */
    public static final String DELAY_RETRY_QUEUE = SUFFIX + "d.retry.queue.%s";
}
