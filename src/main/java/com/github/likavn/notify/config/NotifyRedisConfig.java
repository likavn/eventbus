package com.github.likavn.notify.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.likavn.notify.api.MsgSender;
import com.github.likavn.notify.domain.SubMsgConsumer;
import com.github.likavn.notify.prop.NotifyProperties;
import com.github.likavn.notify.provider.redis.RedisDelayMsgListener;
import com.github.likavn.notify.provider.redis.RedisMsgSender;
import com.github.likavn.notify.provider.redis.RedisSubscribeMsgListener;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.nio.charset.StandardCharsets;

/**
 * 通知配置
 *
 * @author likavn
 * @since 2023/01/01
 */
@Configuration
@ConditionalOnClass(RedisConnectionFactory.class)
@ConditionalOnProperty(prefix = "notify", name = "type", havingValue = "redis")
public class NotifyRedisConfig {

    @Bean
    @ConditionalOnBean(RedisConnectionFactory.class)
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
     * 消息通知rabbitmq实现
     */
    @Bean
    public MsgSender redisMsgSender(
            @Qualifier("notifyRedisTemplate") RedisTemplate<String, String> redisTemplate) {
        return new RedisMsgSender(redisTemplate);
    }

    /**
     * 初始化延时事件消息监听器
     */
    @Bean
    public RedisDelayMsgListener delayMessageListener(
            @Qualifier("notifyRedisTemplate") RedisTemplate<String, String> redisTemplate) {
        return new RedisDelayMsgListener(redisTemplate);
    }

    /**
     * Redis消息监听器容器
     * 这个容器加载了RedisConnectionFactory和消息监听器
     * 可以添加多个监听不同话题的redis监听器，只需要把消息监听器和相应的消息订阅处理器绑定，该消息监听器
     * 通过反射技术调用消息订阅处理器的相关方法进行一些业务处理
     *
     * @param redisConnectionFactory 连接工厂
     * @param adapter                适配器
     * @return redis消息监听容器
     */
    @Bean
    @SuppressWarnings("all")
    public RedisMessageListenerContainer container(RedisConnectionFactory redisConnectionFactory,
                                                   NotifyProperties config) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        // 监听所有库的key过期事件
        container.setConnectionFactory(redisConnectionFactory);
        // 所有的订阅消息，都需要在这里进行注册绑定,new PatternTopic(TOPIC_NAME1)表示发布的主题信息
        // 可以添加多个 messageListener，配置不同的通道
        for (SubMsgConsumer consumer : config.getSubMsgConsumers()) {
            container.addMessageListener(new RedisSubscribeMsgListener(consumer),
                    new PatternTopic(consumer.getTopic()));
        }
        /**
         * 设置序列化对象
         * 特别注意：1. 发布的时候需要设置序列化；订阅方也需要设置序列化
         *         2. 设置序列化对象必须放在[加入消息监听器]这一步后面，否则会导致接收器接收不到消息
         */
        Jackson2JsonRedisSerializer seria = new Jackson2JsonRedisSerializer(Object.class);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        seria.setObjectMapper(objectMapper);
        container.setTopicSerializer(seria);

        return container;
    }

}
