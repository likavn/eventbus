package com.github.likavn.notify.provider.redis;

import com.github.likavn.notify.domain.SubMsgConsumer;
import com.github.likavn.notify.prop.NotifyProperties;
import com.github.likavn.notify.provider.redis.constant.RedisConstant;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;

import java.net.InetAddress;
import java.time.Duration;
import java.util.List;

/**
 * redis消息监听器
 *
 * @author likavn
 * @since 2023/01/01
 **/
@Slf4j
public class RedisSubscribeMsgListener {
    private final NotifyProperties notifyProperties;

    private final List<SubMsgConsumer> subMsgConsumers;

    private final RedisTemplate<String, String> redisTemplate;

    private StreamMessageListenerContainer<String, ObjectRecord<String, String>> listenerContainer;

    public RedisSubscribeMsgListener(RedisConnectionFactory redisConnectionFactory,
                                     NotifyProperties notifyProperties, RedisTemplate<String, String> redisTemplate,
                                     List<SubMsgConsumer> subMsgConsumers) {
        this.notifyProperties = notifyProperties;
        this.subMsgConsumers = subMsgConsumers;
        this.redisTemplate = redisTemplate;
        bindListener(redisConnectionFactory);
    }

    @SneakyThrows
    private void bindListener(RedisConnectionFactory redisConnectionFactory) {
        // 创建配置对象
        var options
                = StreamMessageListenerContainer
                .StreamMessageListenerContainerOptions
                .builder()
                // 一次性最多拉取多少条消息
                .batchSize(notifyProperties.getRedis().getSubBatchSize())
                // 消息消费异常的handler
                .errorHandler(t -> {
                    t.printStackTrace();
                    log.error("[MQ handler exception] " + t.getMessage());
                })
                // 超时时间，设置为0，表示不超时（超时后会抛出异常）
                .pollTimeout(Duration.ZERO)
                // 序列化器
                .serializer(new StringRedisSerializer())
                .targetType(String.class)
                .build();

        // 根据配置对象创建监听容器对象
        listenerContainer = StreamMessageListenerContainer.create(redisConnectionFactory, options);
        for (SubMsgConsumer consumer : subMsgConsumers) {
            // 初始化组
            createStreamGroup(redisTemplate, consumer);

            String groupName = consumer.getListener().getClass().getName();
            // 使用监听容器对象开始监听消费（使用的是手动确认方式）
            listenerContainer.receive(Consumer.from(groupName, InetAddress.getLocalHost().getHostName()),
                    StreamOffset.create(String.format(RedisConstant.NOTIFY_SUBSCRIBE_PREFIX, consumer.getTopic()), ReadOffset.lastConsumed()),
                    message -> consumer.accept(message.getValue()));
        }
        // 启动监听
        listenerContainer.start();
    }

    /**
     * destroy listener Container
     */
    public void destroy() {
        this.listenerContainer.stop();
    }

    /**
     * 创建消费者组
     *
     * @param redisTemplate redisTemplate
     * @param consumer      消费者
     */
    private void createStreamGroup(RedisTemplate<String, String> redisTemplate, SubMsgConsumer consumer) {
        String key = String.format(RedisConstant.NOTIFY_SUBSCRIBE_PREFIX, consumer.getTopic());
        String groupName = consumer.getListener().getClass().getName();
        boolean hasGroup = false;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            StreamInfo.XInfoGroups groups = redisTemplate.opsForStream().groups(key);
            long count = groups.stream().filter(x -> groupName.equals(x.groupName())).count();
            if (count > 0) {
                hasGroup = true;
            }
        }
        if (!hasGroup) {
            redisTemplate.opsForStream().createGroup(key, groupName);
        }
    }
}
