package com.github.likavn.eventbus.provider.redis.support;

import com.github.likavn.eventbus.core.base.NetLifecycle;
import com.github.likavn.eventbus.core.constant.BusConstant;
import com.github.likavn.eventbus.core.utils.Func;
import com.github.likavn.eventbus.prop.BusProperties;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.time.Duration;
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
public abstract class AbstractDefaultStreamContainer implements NetLifecycle {
    protected final StringRedisTemplate stringRedisTemplate;
    protected final BusProperties busProperties;
    protected ThreadPoolExecutor listenerExecutor;
    protected StreamMessageListenerContainer<String, ObjectRecord<String, String>> listenerContainer;

    public AbstractDefaultStreamContainer(StringRedisTemplate stringRedisTemplate, BusProperties busProperties) {
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
                .errorHandler(t -> log.error("[MQ handler exception] ", t))
                // 超时时间，设置为0，表示不超时（超时后会抛出异常）
                .pollTimeout(Duration.ZERO)
                // 序列化器
                .serializer(new StringRedisSerializer())
                .targetType(String.class)
                .build();

        // 根据配置对象创建监听容器对象
        listenerContainer = StreamMessageListenerContainer.create(Objects.requireNonNull(stringRedisTemplate.getConnectionFactory()), options);
        // 添加消费者
        addReceives(listenerContainer);
        // 启动监听
        listenerContainer.start();
    }

    /**
     * 注册消费者
     *
     * @param listenerContainer 监听容器
     */
    protected abstract void addReceives(StreamMessageListenerContainer<String, ObjectRecord<String, String>> listenerContainer);

    @Override
    public void destroy() {
        listenerContainer.stop();
        Func.resetPool(listenerExecutor);
    }
}
