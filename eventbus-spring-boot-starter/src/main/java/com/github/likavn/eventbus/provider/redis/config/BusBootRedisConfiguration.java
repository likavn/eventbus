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
package com.github.likavn.eventbus.provider.redis.config;

import com.github.likavn.eventbus.core.DeliveryBus;
import com.github.likavn.eventbus.core.ListenerRegistry;
import com.github.likavn.eventbus.core.TaskRegistry;
import com.github.likavn.eventbus.core.api.RequestIdGenerator;
import com.github.likavn.eventbus.core.metadata.BusConfig;
import com.github.likavn.eventbus.core.metadata.InterceptorConfig;
import com.github.likavn.eventbus.core.utils.Assert;
import com.github.likavn.eventbus.prop.BusProperties;
import com.github.likavn.eventbus.provider.redis.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisServerCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

import java.util.Properties;

/**
 * redis实现配置
 *
 * @author likavn
 * @date 2024/01/01
 */
@Configuration
@ConditionalOnClass(RedisConnectionFactory.class)
@ConditionalOnProperty(prefix = "eventbus", name = "type", havingValue = "redis")
public class BusBootRedisConfiguration {
    @Bean
    public StringRedisTemplate busStringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }

    @Bean
    public RLock rLock(StringRedisTemplate busStringRedisTemplate, DefaultRedisScript<Boolean> lockRedisScript) {
        checkRedisVersion(busStringRedisTemplate);
        return new RLock(busStringRedisTemplate, lockRedisScript);
    }

    /**
     * 校验 Redis 版本号，是否满足最低的版本号要求！
     */
    private static void checkRedisVersion(StringRedisTemplate busStringRedisTemplate) {
        // 获得 Redis 版本
        Properties info = busStringRedisTemplate.execute((RedisCallback<Properties>) RedisServerCommands::info);
        Assert.notEmpty(info, "Redis 版本信息为空！");
        assert info != null;
        String version = info.getProperty("redis_version");
        // 校验最低版本必须大于等于 6.2
        if (version.contains("-")) {
            version = version.substring(0, version.indexOf("-"));
        }
        boolean isValid = false;
        String[] versions = version.split("\\.");
        String bigVersion = versions[0];
        if (bigVersion.compareTo("6") >= 0
                && (bigVersion.compareTo("7") >= 0 || (versions.length >= 2 && versions[1].compareTo("2") >= 0))) {
            isValid = true;
        }
        if (!isValid) {
            throw new IllegalStateException(String.format("您当前的 Redis 版本为 %s，小于最低要求的 6.2 版本！", version));
        }
    }

    @Bean
    public RedisMsgSubscribeListener redisMsgSubscribeListener(
            StringRedisTemplate busStringRedisTemplate,
            BusProperties busProperties, ListenerRegistry registry, DeliveryBus deliveryBus) {
        return new RedisMsgSubscribeListener(busStringRedisTemplate, busProperties, registry.getTimelyListeners(), deliveryBus);
    }

    @Bean
    public RedisMsgDelayListener redisMsgDelayListener(
            StringRedisTemplate busStringRedisTemplate, TaskRegistry taskRegistry,
            BusProperties busProperties,
            @Qualifier("pushMsgStreamRedisScript") DefaultRedisScript<Long> pushMsgStreamRedisScript, RLock rLock, DeliveryBus deliveryBus) {
        return new RedisMsgDelayListener(busStringRedisTemplate, taskRegistry, busProperties, pushMsgStreamRedisScript, rLock, deliveryBus);
    }

    @Bean
    public RedisMsgSender msgSender(StringRedisTemplate busStringRedisTemplate,
                                    BusConfig config,
                                    InterceptorConfig interceptorConfig,
                                    @Qualifier("zsetAddRedisScript")
                                    DefaultRedisScript<Long> zsetAddRedisScript,
                                    TaskRegistry taskRegistry, RequestIdGenerator requestIdGenerator, ListenerRegistry registry) {
        return new RedisMsgSender(busStringRedisTemplate, config, interceptorConfig, zsetAddRedisScript, taskRegistry, requestIdGenerator, registry);
    }

    /**
     * 任务注册器
     */
    @Bean
    public TaskRegistry taskRegistry() {
        return new TaskRegistry();
    }

    @Bean
    public RedisPendingMsgResendTask redisPendingMsgResendTask(
            StringRedisTemplate busStringRedisTemplate, TaskRegistry taskRegistry,
            BusProperties busProperties, ListenerRegistry registry, RLock rLock, RedisMsgSender msgSender) {
        return new RedisPendingMsgResendTask(busStringRedisTemplate, taskRegistry, busProperties, registry.getTimelyListeners(), rLock, msgSender);
    }

    @Bean
    public RedisStreamExpiredTask redisStreamExpiredTask(
            StringRedisTemplate busStringRedisTemplate, TaskRegistry taskRegistry, BusProperties busProperties, ListenerRegistry registry, RLock rLock) {
        return new RedisStreamExpiredTask(busStringRedisTemplate, taskRegistry, busProperties, registry.getTimelyListeners(), rLock);
    }

    @Bean
    public RedisNodeTestConnect redisNodeTestConnect(StringRedisTemplate busStringRedisTemplate, BusConfig config) {
        return new RedisNodeTestConnect(busStringRedisTemplate, config);
    }

    @Configuration
    public static class ScriptAutoConfiguration {
        /**
         * redis锁脚本
         */
        @Bean
        public DefaultRedisScript<Boolean> lockRedisScript() {
            DefaultRedisScript<Boolean> redisScript = new DefaultRedisScript<>();
            redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("script/lock.lua")));
            redisScript.setResultType(Boolean.class);
            return redisScript;
        }

        /**
         * redis 延时消息添加脚本
         */
        @Bean
        public DefaultRedisScript<Long> zsetAddRedisScript() {
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
            redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("script/zsetAdd.lua")));
            redisScript.setResultType(Long.class);
            return redisScript;
        }

        /**
         * redis推送脚本
         */
        @Bean
        public DefaultRedisScript<Long> pushMsgStreamRedisScript() {
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
            redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("script/pushMsgStream.lua")));
            redisScript.setResultType(Long.class);
            return redisScript;
        }
    }


}
