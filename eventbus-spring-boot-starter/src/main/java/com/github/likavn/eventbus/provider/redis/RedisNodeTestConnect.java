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
package com.github.likavn.eventbus.provider.redis;

import com.github.likavn.eventbus.core.base.NodeTestConnect;
import com.github.likavn.eventbus.core.metadata.BusConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * redis连接状态测试
 *
 * @author likavn
 * @date 2023/5/30
 **/
@Slf4j
public class RedisNodeTestConnect implements NodeTestConnect {

    private final StringRedisTemplate stringRedisTemplate;
    private final String testKey;

    public RedisNodeTestConnect(StringRedisTemplate stringRedisTemplate, BusConfig busConfig) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.testKey = String.format("eventbus.{%s}", busConfig.getServiceId());
    }

    @Override
    public boolean testConnect() {
        try {
            stringRedisTemplate.hasKey(testKey);
            return true;
        } catch (Exception ex) {
            log.error("redis timeout", ex);
            return false;
        }
    }
}
