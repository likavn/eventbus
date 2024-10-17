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

import com.github.likavn.eventbus.ConditionalOnEventbusActive;
import com.github.likavn.eventbus.config.EventBusAutoConfiguration;
import com.github.likavn.eventbus.core.DeliveryBus;
import com.github.likavn.eventbus.core.ListenerRegistry;
import com.github.likavn.eventbus.core.TaskRegistry;
import com.github.likavn.eventbus.core.api.MsgSender;
import com.github.likavn.eventbus.core.api.RequestIdGenerator;
import com.github.likavn.eventbus.core.metadata.BusConfig;
import com.github.likavn.eventbus.core.base.InterceptorContainer;
import com.github.likavn.eventbus.core.metadata.BusType;
import com.github.likavn.eventbus.core.utils.Assert;
import com.github.likavn.eventbus.prop.BusProperties;
import com.github.likavn.eventbus.provider.redis.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
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
@AutoConfigureAfter(EventBusAutoConfiguration.class)
@ConditionalOnClass(RedisConnectionFactory.class)
@ConditionalOnEventbusActive(value = BusType.REDIS)
public class BusBootRedisConfiguration {

    @Bean
    public StringRedisTemplate busStringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }

    @Bean
    @ConditionalOnMissingBean(RLock.class)
    public RLock rLock(StringRedisTemplate busStringRedisTemplate, BusProperties busProperties, @Qualifier("lockRedisScript") DefaultRedisScript<Boolean> lockRedisScript) {
        checkRedisVersion(busStringRedisTemplate, busProperties);
        return new RLock(busStringRedisTemplate, lockRedisScript);
    }

    /**
     * 校验 Redis 版本号，是否满足最低的版本号要求！
     */
    private static void checkRedisVersion(StringRedisTemplate busStringRedisTemplate, BusProperties busProperties) {
        // 获得 Redis 版本
        Properties info = busStringRedisTemplate.execute((RedisCallback<Properties>) RedisServerCommands::info);
        Assert.notEmpty(info, "Redis 版本信息为空！");
        assert info != null;
        String version = info.getProperty("redis_version");
        busProperties.getRedis().setRedisVersion(version);
        // 校验最低版本必须大于等于 5.0.0
        boolean isValid = false;
        String[] versions = version.split("\\.");
        if (versions[0].compareTo("5") >= 0) {
            isValid = true;
        }
        if (!isValid) {
            throw new IllegalStateException(String.format("您当前的 Redis 版本为 %s，小于最低要求的 5.0 版本！", version));
        }
    }

    @Bean
    @ConditionalOnMissingBean(RedisMsgSubscribeListener.class)
    public RedisMsgSubscribeListener redisMsgSubscribeListener(
            StringRedisTemplate busStringRedisTemplate, BusProperties busProperties, ListenerRegistry registry, DeliveryBus deliveryBus) {
        return new RedisMsgSubscribeListener(busStringRedisTemplate, busProperties, registry, deliveryBus);
    }

    /**
     * 任务注册器
     */
    @Bean
    @ConditionalOnMissingBean(TaskRegistry.class)
    public TaskRegistry taskRegistry() {
        return new TaskRegistry();
    }

    @Bean
    @ConditionalOnMissingBean(RedisPendingMsgResendTask.class)
    public RedisPendingMsgResendTask redisPendingMsgResendTask(
            StringRedisTemplate busStringRedisTemplate, TaskRegistry taskRegistry, BusProperties busProperties, ListenerRegistry registry, RLock rLock, MsgSender msgSender) {
        return new RedisPendingMsgResendTask(busStringRedisTemplate, taskRegistry, busProperties, registry, rLock, msgSender);
    }

    @Bean
    @ConditionalOnMissingBean(RedisStreamExpiredTask.class)
    public RedisStreamExpiredTask redisStreamExpiredTask(
            StringRedisTemplate busStringRedisTemplate, TaskRegistry taskRegistry, BusProperties busProperties, ListenerRegistry registry, RLock rLock) {
        return new RedisStreamExpiredTask(busStringRedisTemplate, taskRegistry, busProperties, registry, rLock);
    }

    @Configuration
    @ConditionalOnEventbusActive(value = BusType.REDIS, sender = true)
    static class RedisSenderConfiguration {

        @Bean
        @ConditionalOnMissingBean(RedisMsgSender.class)
        public RedisMsgSender msgSender(StringRedisTemplate busStringRedisTemplate,
                                        BusConfig config,
                                        @Lazy InterceptorContainer interceptorContainer,
                                        @Qualifier("zsetAddRedisScript")
                                        DefaultRedisScript<Long> zsetAddRedisScript,
                                        TaskRegistry taskRegistry, RequestIdGenerator requestIdGenerator, @Lazy ListenerRegistry registry) {
            return new RedisMsgSender(busStringRedisTemplate, config, interceptorContainer, zsetAddRedisScript, taskRegistry, requestIdGenerator, registry);
        }

        @Bean
        @ConditionalOnMissingBean(RedisZSetPushMsgStreamTask.class)
        public RedisZSetPushMsgStreamTask redisZsetPushMsgStreamTask(
                StringRedisTemplate busStringRedisTemplate,TaskRegistry taskRegistry,
                @Qualifier("pushMsgStreamRedisScript")
                DefaultRedisScript<Long> pushMsgStreamRedisScript, RLock rLock, ListenerRegistry registry) {
            return new RedisZSetPushMsgStreamTask(busStringRedisTemplate, taskRegistry,pushMsgStreamRedisScript,rLock,registry);
        }

        @Bean
        @ConditionalOnMissingBean(RedisNodeTestConnect.class)
        public RedisNodeTestConnect redisNodeTestConnect(StringRedisTemplate busStringRedisTemplate, BusConfig config) {
            return new RedisNodeTestConnect(busStringRedisTemplate, config);
        }
    }

    @Configuration
    static class ScriptAutoConfiguration {
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
