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
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 配置
 *
 * @author likavn
 * @date 2024/01/01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ConfigurationProperties(prefix = "eventbus")
public class BusProperties extends BusConfig {

    /**
     * redis配置
     */
    private RedisProperties redis = new RedisProperties();

    /**
     * redis配置
     */
    @Data
    public static class RedisProperties {
        /**
         * 消息超时时间，超时消息未被确认，才会被重新投递，单位：秒，默认5分钟。
         */
        private Long deliverTimeout = 60 * 5L;
        /**
         * 未确认消息，重新投递时每次最多拉取多少条待确认消息数据，默认：100条；
         */
        private Integer pendingMessagesBatchSize = 100;
        /**
         * stream 过期时间，6.2及以上版本支持，单位：小时，默认 5 天
         */
        private Long streamExpiredHours = 24 * 5L;
    }
}
