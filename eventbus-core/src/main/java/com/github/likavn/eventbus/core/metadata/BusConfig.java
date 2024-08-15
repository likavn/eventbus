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
import lombok.ToString;

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
    protected String serviceId;

    /**
     * 消息引擎类别（redis、rabbitmq、rocketmq）
     */
    protected String type;

    /**
     * 消息接收并发数，默认为：3
     */
    protected Integer concurrency = 3;

    /**
     * 单次获取消息数量，默认：16条
     */
    protected Integer msgBatchSize = 16;

    /**
     * 是否在发送本服务（发送消息的serviceId）及时消息时将及时消息投递到延时队列，默认为：true
     */
    protected Boolean sendTimelyToDelay = true;

    /**
     * 节点联通性配置
     */
    protected TestConnect testConnect = new TestConnect();

    /**
     * 消息投递失败时配置信息
     */
    protected Fail fail = new Fail();

    /**
     * 节点联通性配置
     */
    @Data
    @ToString
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
    @ToString
    public static class Fail {
        /**
         * 消息投递失败时，一定时间内再次进行投递的次数，默认：3次
         */
        private Integer retryCount = 3;

        /**
         * 下次触发时间，单位：秒，默认10秒 ，（rocketMq为18个延时消息级别）
         */
        private Long nextTime = 10L;
    }
}
