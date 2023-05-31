package com.github.likavn.notify.provider.redis.config;

import com.github.likavn.notify.api.MsgSender;
import com.github.likavn.notify.domain.ServiceContext;
import com.github.likavn.notify.prop.NotifyProperties;
import com.github.likavn.notify.provider.redis.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.nio.charset.StandardCharsets;

/**
 * 通知配置
 *
 * @author likavn
 * @since 2023/01/01
 */
@Configuration
@SuppressWarnings("all")
@ConditionalOnProperty(prefix = "notify", name = "type", havingValue = "redis")
public class NotifyRedisConfig {

    @Bean
    public RedisTemplate<String, String> notifyRedisTemplate(RedisConnectionFactory factory) {
        StringRedisSerializer valueSerializer = new StringRedisSerializer(StandardCharsets.UTF_8);
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setKeySerializer(new StringRedisSerializer());
        template.setDefaultSerializer(new StringRedisSerializer());
        template.setValueSerializer(valueSerializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(valueSerializer);

        template.setConnectionFactory(factory);
        return template;
    }

    /**
     * 消息通知redis实现
     */
    @Bean
    public MsgSender redisMsgSender(@Qualifier("notifyRedisTemplate") RedisTemplate<String, String> redisTemplate) {
        return new RedisMsgSender(redisTemplate);
    }

    /**
     * 初始化延时事件消息监听器
     */
    @Bean
    public RLock rLock(@Qualifier("notifyRedisTemplate") RedisTemplate<String, String> redisTemplate) {
        return new RLock(redisTemplate);
    }

    /**
     * 初始化延时事件消息监听器
     */
    @Bean
    public RedisDelayMsgListener delayMessageListener(
            @Qualifier("notifyRedisTemplate") RedisTemplate<String, String> redisTemplate, RLock rLock, NotifyProperties notifyProperties) {
        return new RedisDelayMsgListener(redisTemplate, rLock, notifyProperties);
    }

    @Bean
    public RedisSubscribeMsgListener redisSubscribeMsgListener(
            RedisConnectionFactory redisConnectionFactory, @Qualifier("notifyRedisTemplate") RedisTemplate<String, String> redisTemplate,
            ServiceContext serviceContext, RLock rLock, NotifyProperties notifyProperties) {
        return new RedisSubscribeMsgListener(redisConnectionFactory,
                notifyProperties, redisTemplate, serviceContext.getSubMsgConsumers(), rLock);
    }

    @Bean
    public RedisNodeTestConnect redisNodeTestConnect(@Qualifier("notifyRedisTemplate") RedisTemplate<String, String> redisTemplate) {
        return new RedisNodeTestConnect(redisTemplate);
    }
}
