package com.github.likavn.eventbus.provider.redis.config;

import com.github.likavn.eventbus.core.DeliveryBus;
import com.github.likavn.eventbus.core.SubscriberRegistry;
import com.github.likavn.eventbus.core.constant.BusConstant;
import com.github.likavn.eventbus.core.metadata.BusConfig;
import com.github.likavn.eventbus.core.metadata.InterceptorConfig;
import com.github.likavn.eventbus.core.utils.NamedThreadFactory;
import com.github.likavn.eventbus.prop.BusProperties;
import com.github.likavn.eventbus.provider.redis.*;
import com.github.likavn.eventbus.schedule.ScheduledTaskRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scripting.support.ResourceScriptSource;

/**
 * redis实现配置
 *
 * @author likavn
 * @date 2024/01/01
 */
@EnableScheduling
@Configuration
@ConditionalOnClass(RedisConnectionFactory.class)
@ConditionalOnProperty(prefix = "eventbus", name = "type", havingValue = "redis")
public class BusBootRedisConfig {

    @Bean
    public StringRedisTemplate busStringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }

    @Bean
    public RedisMsgSender msgSender(InterceptorConfig interceptorConfig, BusConfig config, StringRedisTemplate busStringRedisTemplate) {
        return new RedisMsgSender(interceptorConfig, config, busStringRedisTemplate);
    }

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
     * redis推送脚本
     */
    @Bean
    public DefaultRedisScript<Void> pushMsgStreamRedisScript() {
        DefaultRedisScript<Void> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("script/pushMsgStream.lua")));
        redisScript.setResultType(Void.class);
        return redisScript;
    }

    @Bean
    public RLock rLock(StringRedisTemplate busStringRedisTemplate, DefaultRedisScript<Boolean> lockRedisScript) {
        return new RLock(busStringRedisTemplate, lockRedisScript);
    }

    @Bean
    public RedisMsgSubscribeListener redisMsgSubscribeListener(
            StringRedisTemplate busStringRedisTemplate,
            BusProperties busProperties, SubscriberRegistry registry, DeliveryBus deliveryBus) {
        return new RedisMsgSubscribeListener(busStringRedisTemplate, busProperties, registry.getSubscribers(), deliveryBus);
    }


    @Bean
    public ThreadPoolTaskScheduler busThreadPoolTaskScheduler() {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(2);
        taskScheduler.setRemoveOnCancelPolicy(true);
        taskScheduler.setThreadFactory(new NamedThreadFactory(BusConstant.TASK_NAME));
        return taskScheduler;
    }

    @Bean
    public ScheduledTaskRegistry scheduledTaskRegistry(ThreadPoolTaskScheduler busThreadPoolTaskScheduler) {
        return new ScheduledTaskRegistry(busThreadPoolTaskScheduler);
    }

    @Bean
    public RedisMsgDelayListener redisMsgDelayListener(
            StringRedisTemplate stringRedisTemplate, ScheduledTaskRegistry taskRegistry,
            BusProperties busProperties, DefaultRedisScript<Void> pushMsgStreamRedisScript, RLock rLock, DeliveryBus deliveryBus) {
        return new RedisMsgDelayListener(stringRedisTemplate, taskRegistry, busProperties, pushMsgStreamRedisScript, rLock, deliveryBus);
    }

    @Bean
    public RedisPendingMsgResendTask redisPendingMsgResendTask(
            StringRedisTemplate stringRedisTemplate, ScheduledTaskRegistry scheduledTaskRegistry,
            BusProperties busProperties, SubscriberRegistry registry, RLock rLock, RedisMsgSender msgSender) {
        return new RedisPendingMsgResendTask(stringRedisTemplate, scheduledTaskRegistry, busProperties, registry.getSubscribers(), rLock, msgSender);
    }

    @Bean
    public RedisNodeTestConnect redisNodeTestConnect(StringRedisTemplate stringRedisTemplate, BusConfig config) {
        return new RedisNodeTestConnect(stringRedisTemplate, config);
    }
}
