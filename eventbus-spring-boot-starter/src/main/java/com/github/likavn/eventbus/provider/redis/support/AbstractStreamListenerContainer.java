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
package com.github.likavn.eventbus.provider.redis.support;

import com.github.likavn.eventbus.core.base.Lifecycle;
import com.github.likavn.eventbus.core.utils.Func;
import com.github.likavn.eventbus.prop.BusProperties;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.util.Assert;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * redis stream消息监听容器
 *
 * @author likavn
 * @date 2024/1/9
 **/
@Slf4j
public abstract class AbstractStreamListenerContainer implements Lifecycle {
    protected final StringRedisTemplate redisTemplate;
    protected final BusProperties config;
    protected final String threadName;
    protected ThreadPoolExecutor executor;
    protected StreamMessageListenerContainer<String, ObjectRecord<String, String>> container;

    protected AbstractStreamListenerContainer(StringRedisTemplate redisTemplate, BusProperties config, String threadName) {
        this.redisTemplate = redisTemplate;
        this.config = config;
        this.threadName = threadName;
    }

    @Override
    public void register() {
        if (null != container) {
            container.start();
            return;
        }
        executor = new ThreadPoolExecutor(
                config.getRedis().getExecutorPoolSize(),
                config.getRedis().getExecutorPoolSize(),
                1,
                TimeUnit.MINUTES,
                new LinkedBlockingDeque<>(),
                new CustomizableThreadFactory(threadName));
        // 创建配置对象
        var options
                = StreamMessageListenerContainer.StreamMessageListenerContainerOptions.builder()
                .executor(executor)
                // 一次性最多拉取多少条消息
                .batchSize(config.getRedis().getBatchSize())
                // 消息消费异常的handler
                .errorHandler(t -> log.error("[Eventbus error] ", t))
                // 超时时间，设置为0，表示不超时（超时后会抛出异常）
                .pollTimeout(Duration.ZERO)
                // 序列化器
                .serializer(new StringRedisSerializer())
                .targetType(String.class)
                .build();

        // 根据配置对象创建监听容器对象
        RedisConnectionFactory connectionFactory = redisTemplate.getConnectionFactory();
        Assert.notNull(connectionFactory, "RedisConnectionFactory must not be null!");
        container = StreamMessageListenerContainer.create(connectionFactory, options);
        // 添加消费者
        createConsumer(container);
        // 启动监听
        container.start();
    }

    /**
     * 注册消费者
     *
     * @param container 监听容器
     */
    private void createConsumer(StreamMessageListenerContainer<String, ObjectRecord<String, String>> container) {
        List<RedisSubscriber> subscribers = getSubscribers();
        String hostName = Func.getHostName() + "@" + Func.getPid();
        // 初始化组
        createGroup(subscribers);
        subscribers.forEach(subscriber -> {
            int num = 0;
            while (++num <= config.getConsumerCount()) {
                // 使用监听容器对象开始监听消费（使用的是手动确认方式）
                container.receive(Consumer.from(subscriber.getGroup(), hostName + "-" + num),
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
        if (null != container) {
            container.stop();
        }
        Func.resetPool(executor);
    }

    /**
     * 创建消费者组。
     * 遍历给定的订阅者列表，为每个订阅者所指定的流创建消费者组，如果该消费者组还未在对应的流上存在的话。
     *
     * @param subscribers 订阅者列表，每个订阅者包含流的键和消费者组的名称。
     */
    private void createGroup(List<RedisSubscriber> subscribers) {
        // 根据流的键对订阅者进行分组
        subscribers.stream().collect(Collectors.groupingBy(RedisSubscriber::getStreamKey))
                .forEach((streamKey, subs) -> {
                    // 检查流的键是否在Redis中存在
                    if (Boolean.TRUE.equals(redisTemplate.hasKey(streamKey))) {
                        // 获取当前流上已存在的消费者组信息
                        StreamInfo.XInfoGroups groups = redisTemplate.opsForStream().groups(streamKey);
                        // 从订阅者列表中移除那些组名已经存在于流上的订阅者
                        subs = subs.stream().filter(t -> {
                            long count = groups.stream().filter(g -> g.groupName().equals(t.getGroup())).count();
                            return count <= 0;
                        }).collect(Collectors.toList());
                    }
                    // 为剩余的订阅者（即组名在流上不存在的订阅者）创建新的消费者组
                    if (!subs.isEmpty()) {
                        subs.forEach(t -> redisTemplate.opsForStream().createGroup(t.getStreamKey(), t.getGroup()));
                    }
                });
    }
}
