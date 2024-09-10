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
    private static final String SUFFIX = "eventbus.";

    /**
     * rabbitMq及时消息交换机
     * 参数：
     * <p>
     * 1.服务serviceId;
     */
    public static final String EXCHANGE = SUFFIX + "exchange.%s";

    /**
     * rabbitMq路由key；
     * 参数：
     * <p>
     * 1.Topic;
     */
    public static final String ROUTING_KEY = SUFFIX + "routingKey.%s";

    /**
     * rabbitMq队列key；
     * 参数：
     * <p>
     * 1.Topic;
     * 2.处理器全类名+方法名ID;
     */
    public static final String QUEUE = SUFFIX + "queue.%s@%s";

    /**
     * 延时队列交换机
     * 参数：
     * <p>
     * 1.服务serviceId;
     */
    public static final String DELAY_EXCHANGE = SUFFIX + "delayExchange.%s";

    /**
     * 延时队列路由key
     * 参数：
     * <p>
     * 1.服务serviceId;
     */
    public static final String DELAY_ROUTING_KEY = SUFFIX + "delayRoutingKey.%s";

    /**
     * 延时队列名
     * 参数：
     * <p>
     * 1.服务serviceId;
     */
    public static final String DELAY_QUEUE = SUFFIX + "delayQueue.%s@%s";
}
