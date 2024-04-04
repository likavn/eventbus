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
package com.github.likavn.eventbus.prop;

import com.github.likavn.eventbus.core.metadata.BusConfig;
import lombok.Data;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 配置
 *
 * @author likavn
 * @date 2024/01/01
 */
@Data
@ToString
@ConfigurationProperties(prefix = "eventbus")
public class BusProperties {
    /**
     * 服务ID
     */
    private String serviceId;

    /**
     * 消息引擎类别（redis、rabbitmq、rocketmq、pulsar）
     */
    private String type;

    /**
     * 消费者数量
     */
    private Integer consumerCount = 1;

    /**
     * 节点联通性配置
     */
    private BusConfig.TestConnect testConnect = new BusConfig.TestConnect();

    /**
     * 消息投递失败时配置信息
     */
    private BusConfig.Fail fail = new BusConfig.Fail();

    /**
     * redis配置
     */
    private RedisProperties redis = new RedisProperties();

    /**
     * redis配置
     */
    @Data
    @ToString
    public static class RedisProperties {
        /**
         * 订阅消息线程池大小
         */
        private Integer executorPoolSize = 10;
        /**
         * 消息超时时间，默认 5 分钟
         * 1. 超时的消息才会被重新投递
         * 2. 由于定时任务 1 分钟一次，消息超时后不会被立即重投，极端情况下消息5分钟过期后，再等 1 分钟才会被扫瞄到
         */
        private Long deliverTimeout = 60 * 5L;
        /**
         * 订阅消息一次性最多拉取多少条消息数据
         */
        private Integer batchSize = 10;
        /**
         * 每次最多拉取多少条待确认消息数据
         */
        private Integer pendingMessagesBatchSize = 100;
        /**
         * stream 过期时间，单位：小时，默认 7 天
         */
        private Long streamExpiredHours = 24 * 7L;
    }
}
