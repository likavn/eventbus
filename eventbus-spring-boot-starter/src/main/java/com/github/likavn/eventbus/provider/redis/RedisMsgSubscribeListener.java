package com.github.likavn.eventbus.provider.redis;

import com.github.likavn.eventbus.core.DeliveryBus;
import com.github.likavn.eventbus.core.base.NetLifecycle;
import com.github.likavn.eventbus.core.constant.BusConstant;
import com.github.likavn.eventbus.core.metadata.support.Subscriber;
import com.github.likavn.eventbus.core.utils.Func;
import com.github.likavn.eventbus.prop.BusProperties;
import com.github.likavn.eventbus.provider.redis.support.RedisSubscriber;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.net.InetAddress;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * redis消息监听器
 *
 * @author likavn
 * @since 2023/01/01
 **/
@Slf4j
public class RedisMsgSubscribeListener implements NetLifecycle {
    private final BusProperties busProperties;
    private final BusProperties.RedisProperties redisProperties;
    private final List<RedisSubscriber> subscribers;
    private final DeliveryBus deliveryBus;
    private final RedisTemplate<String, String> redisTemplate;
    private StreamMessageListenerContainer<String, ObjectRecord<String, String>> listenerContainer;
    private ThreadPoolExecutor listenerExecutor;

    public RedisMsgSubscribeListener(BusProperties busProperties,
                                     List<Subscriber> subscribers,
                                     DeliveryBus deliveryBus,
                                     RedisTemplate<String, String> redisTemplate) {
        this.busProperties = busProperties;
        this.redisProperties = busProperties.getRedis();
        this.subscribers = subscribers.stream().map(RedisSubscriber::new).collect(Collectors.toList());
        this.deliveryBus = deliveryBus;
        this.redisTemplate = redisTemplate;
        try {
            //   register();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void register() throws Exception {
        if (null != listenerContainer) {
            listenerContainer.start();
            return;
        }
        listenerExecutor = new ThreadPoolExecutor(
                redisProperties.getExecutorPoolSize(),
                redisProperties.getExecutorPoolSize(),
                1,
                TimeUnit.MINUTES,
                new LinkedBlockingDeque<>(),
                new CustomizableThreadFactory(BusConstant.SUBSCRIBE_MSG_THREAD_NAME));
        // 创建配置对象
        var options
                = StreamMessageListenerContainer
                .StreamMessageListenerContainerOptions
                .builder()
                .executor(listenerExecutor)
                // 一次性最多拉取多少条消息
                .batchSize(redisProperties.getBatchSize())
                // 消息消费异常的handler
                .errorHandler(t -> log.error("[MQ handler exception] ", t))
                // 超时时间，设置为0，表示不超时（超时后会抛出异常）
                .pollTimeout(Duration.ZERO)
                // 序列化器
                .serializer(new StringRedisSerializer())
                .targetType(String.class)
                .build();

        // 根据配置对象创建监听容器对象
        listenerContainer = StreamMessageListenerContainer.create(redisTemplate.getConnectionFactory(), options);
        for (RedisSubscriber subscriber : subscribers) {
            // 初始化组
            createConsumerGroup(redisTemplate, subscriber);

            Integer num = 1;
            while (num++ <= busProperties.getConsumerNum()) {
                // 使用监听容器对象开始监听消费（使用的是手动确认方式）
                listenerContainer.receive(Consumer.from(subscriber.getGroup(), InetAddress.getLocalHost().getHostName() + "-" + num),
                        StreamOffset.create(subscriber.getStreamKey(), ReadOffset.lastConsumed()),
                        msg -> {
                            deliveryBus.deliver(subscriber, msg.getValue());
                            redisTemplate.opsForStream().acknowledge(subscriber.getStreamKey(), subscriber.getGroup(), msg.getId());
                        });
            }
        }
        // 启动监听
        listenerContainer.start();
    }

    /**
     * 创建消费者组
     *
     * @param redisTemplate redisTemplate
     * @param consumer      消费者
     */
    public static void createConsumerGroup(RedisTemplate<String, String> redisTemplate, RedisSubscriber consumer) {
        boolean hasGroup = false;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(consumer.getStreamKey()))) {
            StreamInfo.XInfoGroups groups = redisTemplate.opsForStream().groups(consumer.getStreamKey());
            long count = groups.stream().filter(x -> consumer.getGroup().equals(x.groupName())).count();
            if (count > 0) {
                hasGroup = true;
            }
        }
        if (!hasGroup) {
            redisTemplate.opsForStream().createGroup(consumer.getStreamKey(), consumer.getGroup());
        }
    }

    /**
     * destroy listener Container
     */
    @Override
    public void destroy() {
        listenerContainer.stop();
        Func.resetPool(listenerExecutor);
    }
}
