package com.github.likavn.eventbus.provider.redis.support;

import com.github.likavn.eventbus.core.base.NetLifecycle;
import com.github.likavn.eventbus.core.constant.BusConstant;
import com.github.likavn.eventbus.core.utils.Func;
import com.github.likavn.eventbus.prop.BusProperties;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * redis stream消息监听容器
 *
 * @author likavn
 * @date 2024/1/9
 **/
@Slf4j
public abstract class AbstractStreamListenerContainer implements NetLifecycle {
    protected final StringRedisTemplate stringRedisTemplate;
    protected final BusProperties busProperties;
    protected ThreadPoolExecutor listenerExecutor;
    protected StreamMessageListenerContainer<String, ObjectRecord<String, String>> listenerContainer;

    public AbstractStreamListenerContainer(StringRedisTemplate stringRedisTemplate, BusProperties busProperties) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.busProperties = busProperties;
    }

    @Override
    public void register() {
        if (null != listenerContainer) {
            listenerContainer.start();
            return;
        }
        listenerExecutor = new ThreadPoolExecutor(
                busProperties.getRedis().getExecutorPoolSize(),
                busProperties.getRedis().getExecutorPoolSize(),
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
                .batchSize(busProperties.getRedis().getBatchSize())
                // 消息消费异常的handler
                .errorHandler(t -> log.error("[Eventbus handler exception] ", t))
                // 超时时间，设置为0，表示不超时（超时后会抛出异常）
                .pollTimeout(Duration.ZERO)
                // 序列化器
                .serializer(new StringRedisSerializer())
                .targetType(String.class)
                .build();

        // 根据配置对象创建监听容器对象
        listenerContainer = StreamMessageListenerContainer.create(Objects.requireNonNull(stringRedisTemplate.getConnectionFactory()), options);
        // 添加消费者
        createConsumer(listenerContainer);
        // 启动监听
        listenerContainer.start();
    }

    /**
     * 注册消费者
     *
     * @param listenerContainer 监听容器
     */
    private void createConsumer(StreamMessageListenerContainer<String, ObjectRecord<String, String>> listenerContainer) {
        List<RedisSubscriber> subscribers = getSubscribers();
        String hostName = Func.getHostName();
        subscribers.forEach(subscriber -> {
            // 初始化组
            createConsumerGroup(subscriber);
            int num = 1;
            while (num++ <= busProperties.getConsumerCount()) {
                // 使用监听容器对象开始监听消费（使用的是手动确认方式）
                listenerContainer.receive(Consumer.from(subscriber.getGroup(), hostName + "-" + num),
                        StreamOffset.create(subscriber.getStreamKey(), ReadOffset.lastConsumed()),
                        msg -> deliver(subscriber, msg));
            }
        });
    }

    /**
     * 获取消费者
     *
     * @return 消费者
     */
    protected abstract List<RedisSubscriber> getSubscribers();

    /**
     * 消费消息
     *
     * @param subscriber 消费者
     * @param msg        消息体
     */
    protected abstract void deliver(RedisSubscriber subscriber, Record<String, String> msg);

    @Override
    public void destroy() {
        listenerContainer.stop();
        Func.resetPool(listenerExecutor);
    }

    /**
     * 创建消费者组
     *
     * @param consumer 消费者
     */
    private void createConsumerGroup(RedisSubscriber consumer) {
        createConsumerGroup(consumer.getStreamKey(), consumer.getGroup());
    }

    /**
     * 创建消费者组
     *
     * @param streamKey streamKey
     * @param group     group
     */
    private void createConsumerGroup(String streamKey, String group) {
        boolean hasGroup = false;
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(streamKey))) {
            StreamInfo.XInfoGroups groups = stringRedisTemplate.opsForStream().groups(streamKey);
            long count = groups.stream().filter(x -> group.equals(x.groupName())).count();
            if (count > 0) {
                hasGroup = true;
            }
        }
        if (!hasGroup) {
            stringRedisTemplate.opsForStream().createGroup(streamKey, group);
        }
    }
}
