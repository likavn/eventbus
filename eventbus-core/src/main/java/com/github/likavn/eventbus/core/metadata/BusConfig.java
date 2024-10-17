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
package com.github.likavn.eventbus.core.metadata;

import lombok.Data;

/**
 * eventbus全局配置
 *
 * @author likavn
 * @date 2024/01/01
 */
@Data
public class BusConfig {
    /**
     * 服务ID/消息来源ID，可以不用配置，默认为：spring.application.name
     */
    private String serviceId;

    /**
     * 消息引擎类别（redis、rabbitmq、rocketmq）
     * {@link com.github.likavn.eventbus.core.metadata.BusType}
     */
    private String type;

    /**
     * 原消息引擎类别（redis、rabbitmq、rocketmq），用于消息引擎切换时兼容原始消息，消息引擎没做迁移时可不做配置
     * 默认等于={@link #type}
     * {@link com.github.likavn.eventbus.core.metadata.BusType}
     */
    private String oldType;

    /**
     * 消息接收并发数，默认为：2
     */
    private Integer concurrency = 2;

    /**
     * 重试消息接收并发数，默认为：1
     */
    private Integer retryConcurrency = 1;

    /**
     * 单次获取消息数量，默认：16条
     */
    private Integer msgBatchSize = 16;

    /**
     * 节点联通性配置
     */
    private TestConnect testConnect = new TestConnect();

    /**
     * 消息投递失败时配置信息
     */
    private Fail fail = new Fail();

    /**
     * 节点联通性配置
     */
    @Data
    public static class TestConnect {
        /**
         * 轮询检测时间间隔，单位：秒，默认：35秒进行检测一次
         */
        private Long pollSecond = 35L;

        /**
         * 丢失连接最长时间大于等于次值设置监听容器为连接断开，单位：秒，默认：120秒
         */
        private Long loseConnectMaxMilliSecond = 120L;
    }

    /**
     * 消息投递失败时配置
     */
    @Data
    public static class Fail {
        /**
         * 消息投递失败时，一定时间内再次进行投递的次数，默认：3次
         */
        private Integer retryCount = 3;

        /**
         * 下次触发时间，单位：秒，默认10秒 ，（rocketMq对应为18个延时消息级别）
         */
        private Long nextTime = 10L;
    }
}
