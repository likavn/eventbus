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
package com.github.likavn.eventbus.provider.redis.constant;

/**
 * redis常量
 *
 * @author likavn
 * @since 2023/01/01
 */
public class RedisConstant {
    private RedisConstant() {
    }

    /**
     * 前缀
     */
    private static final String SUFFIX = "ebus:";

    /**
     * 及时消息队列,stream key
     * 参数：
     * <p>
     * 1. 服务ID|消息编码
     */
    public static final String TIMELY_QUEUE = SUFFIX + "t:queue:{%s}";

    /**
     * 及时消息重试,zset key
     * 参数：
     * <p>
     * 1.服务ID|消息编码|消息监听器类全类名
     */
    public static final String TIMELY_RETRY_ZSET = SUFFIX + "t:retry:zset:{%s}";

    /**
     * 及时消息重试,lock key
     * 参数：
     * <p>
     * 1.服务ID|消息编码|消息监听器类全类名
     */
    public static final String TIMELY_RETRY_LOCK = SUFFIX + "t:retry:lock:{%s}";

    /**
     * 及时消息重试,stream key
     * 参数：
     * <p>
     * 1.服务ID|消息编码|消息监听器类全类名
     */
    public static final String TIMELY_RETRY_QUEUE = SUFFIX + "t:retry:queue:{%s}";

    /**
     * 延时消息，zset key
     * 参数：
     * <p>
     * 1.服务ID|消息编码
     */
    public static final String DELAY_ZSET = SUFFIX + "d:zset:{%s}";
    /**
     * 延时消息，lock key
     * 参数：
     * <p>
     * 1.服务ID|消息编码
     */
    public static final String DELAY_LOCK = SUFFIX + "d:lock:{%s}";
    /**
     * 延时消息，stream key
     * 参数：
     * <p>
     * 1.服务ID|消息编码
     */
    public static final String DELAY_QUEUE = SUFFIX + "d:queue:{%s}";

    /**
     * 延时消息重试，zset key
     * 参数：
     * <p>
     * 1.服务ID|消息编码|消息监听器类全类名
     */
    public static final String DELAY_RETRY_ZSET = SUFFIX + "d:retry:zset:{%s}";
    /**
     * 延时消息重试，lock key
     * 参数：
     * <p>
     * 1.服务ID|消息编码|消息监听器类全类名
     */
    public static final String DELAY_RETRY_LOCK = SUFFIX + "d:retry:lock:{%s}";
    /**
     * 延时消息重试，stream key
     * 参数：
     * <p>
     * 1.服务ID|消息编码|消息监听器类全类名
     */
    public static final String DELAY_RETRY_QUEUE = SUFFIX + "d:retry:queue:{%s}";

}
