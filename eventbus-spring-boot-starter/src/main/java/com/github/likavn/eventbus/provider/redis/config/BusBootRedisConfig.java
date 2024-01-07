package com.github.likavn.eventbus.provider.redis.config;

import com.github.likavn.eventbus.core.DeliveryBus;
import com.github.likavn.eventbus.core.SubscriberRegistry;
import com.github.likavn.eventbus.core.api.MsgSender;
import com.github.likavn.eventbus.core.metadata.BusConfig;
import com.github.likavn.eventbus.core.metadata.InterceptorConfig;
import com.github.likavn.eventbus.prop.BusProperties;
import com.github.likavn.eventbus.provider.redis.*;
import com.github.likavn.eventbus.provider.redis.constant.RedisConstant;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

/**
 * redis实现配置
 *
 * @author likavn
 * @date 2024/01/01
 */
@Configuration
@ConditionalOnClass(RedisTemplate.class)
@ConditionalOnProperty(prefix = "eventbus", name = "type", havingValue = "redis")
public class BusBootRedisConfig {

    @Bean
    public RedisLuaManage redisLuaManage() {
        return new RedisLuaManage(RedisConstant.LOCK_LUA, RedisConstant.PUSH_MSG_STREAM_LUA);
    }

    @Bean
    public DefaultRedisScript<Integer> lockDefaultRedisScript() {
        DefaultRedisScript<Integer> lockScript = new DefaultRedisScript<>();
        lockScript.setResultType(Integer.class);
        lockScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("/scripts/lock.lua")));
        return lockScript;
    }

    @Bean
    public MsgSender msgSender(InterceptorConfig interceptorConfig, BusConfig config, RedisTemplate<String, String> redisTemplate) {
        return new RedisMsgSender(interceptorConfig, config, redisTemplate);
    }

    @Bean
    public RLock rLock(RedisTemplate<String, String> redisTemplate, DefaultRedisScript<Integer> lockDefaultRedisScript) {
        return new RLock(redisTemplate, lockDefaultRedisScript);
    }

    @Bean
    public RedisMsgSubscribeListener redisMsgSubscribeListener(
            BusProperties busProperties, SubscriberRegistry registry, DeliveryBus deliveryBus, RedisTemplate<String, String> redisTemplate) {
        return new RedisMsgSubscribeListener(busProperties, registry.getSubscribers(), deliveryBus, redisTemplate);
    }

    @Bean
    public RedisPendingMsgResendTask rPendingMsgResendTask(
            BusProperties busProperties, SubscriberRegistry registry, RLock rLock, MsgSender msgSender, RedisTemplate<String, String> redisTemplate) {
        return new RedisPendingMsgResendTask(busProperties, registry.getSubscribers(), rLock, msgSender, redisTemplate);
    }

    @Bean
    public RedisMsgDelayListener redisMsgDelayListener(
            RedisTemplate<String, String> redisTemplate, BusProperties busProperties, RLock rLock) {
        return new RedisMsgDelayListener(redisTemplate, busProperties, rLock);
    }

    @Bean
    public RedisNodeTestConnect redisNodeTestConnect(RedisTemplate<String, String> redisTemplate) {
        return new RedisNodeTestConnect(redisTemplate);
    }
}
