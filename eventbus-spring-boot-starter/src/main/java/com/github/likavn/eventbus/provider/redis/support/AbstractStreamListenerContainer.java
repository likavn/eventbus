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
import com.github.likavn.eventbus.core.constant.BusConstant;
import com.github.likavn.eventbus.core.utils.Func;
import com.github.likavn.eventbus.core.utils.NamedThreadFactory;
import com.github.likavn.eventbus.prop.BusProperties;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
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
    protected ThreadPoolExecutor executor;
    protected StreamMessageListenerContainer<String, ObjectRecord<String, String>> container;

    protected AbstractStreamListenerContainer(StringRedisTemplate redisTemplate, BusProperties config) {
        this.redisTemplate = redisTemplate;
        this.config = config;
    }

    @Override
    public void register() {
        if (null != container) {
            container.start();
            return;
        }
        List<RedisListener> listeners = getListeners();
        if (Func.isEmpty(listeners)) {
            return;
        }
        int poolSize = listeners.stream().map(RedisListener::getConcurrency).reduce(Integer::sum).orElse(0);
        executor = new ThreadPoolExecutor(poolSize, poolSize, 1,
                TimeUnit.MINUTES, new LinkedBlockingDeque<>(), new NamedThreadFactory(this.getClass().getSimpleName() + "-"));
        // 创建配置对象
        var options = StreamMessageListenerContainer.StreamMessageListenerContainerOptions.builder()
                .executor(executor)
                // 一次性最多拉取多少条消息
                .batchSize(config.getMsgBatchSize())
                // 消息消费异常的handler
                .errorHandler(t -> log.error("[Eventbus error] ", t))
                // 超时时间，设置为0，表示不超时（超时后会抛出异常）
                .pollTimeout(Duration.ofSeconds(60))
                // 序列化器
                .serializer(new StringRedisSerializer())
                .targetType(String.class)
                .build();

        // 根据配置对象创建监听容器对象
        RedisConnectionFactory connectionFactory = redisTemplate.getConnectionFactory();
        Assert.notNull(connectionFactory, "RedisConnectionFactory must not be null!");
        container = StreamMessageListenerContainer.create(connectionFactory, options);
        // 添加消费者
        createConsumer(container, listeners);
        // 启动监听
        container.start();
    }

    /**
     * 注册消费者
     *
     * @param container 监听容器
     * @param listeners listeners
     */
    private void createConsumer(StreamMessageListenerContainer<String, ObjectRecord<String, String>> container, List<RedisListener> listeners) {
        String hostAddress = Func.getHostAddress();
        // 初始化组
        createGroup(listeners);
        for (RedisListener listener : listeners) {
            Func.pollRun(listener.getConcurrency(), () -> container.receive(
                    Consumer.from(listener.getGroup(), hostAddress),
                    StreamOffset.create(listener.getStreamKey(), ReadOffset.lastConsumed()),
                    // 使用监听容器对象开始监听消费（使用的是手动确认方式）
                    msg -> deliverMsg(listener, msg)));
        }
    }

    /**
     * 消费消息
     *
     * @param listener listeners
     * @param msg      msg
     */
    private void deliverMsg(RedisListener listener, Record<String, String> msg) {
        String oldName = Func.reThreadName(BusConstant.THREAD_NAME);
        try {
            deliver(listener, msg);
            redisTemplate.opsForStream().acknowledge(listener.getStreamKey(), listener.getGroup(), msg.getId());
        } finally {
            // 恢复线程名称
            Thread.currentThread().setName(oldName);
        }
    }

    /**
     * 获取消费者
     *
     * @return 消费者
     */
    protected abstract List<RedisListener> getListeners();

    /**
     * 消费消息
     *
     * @param subscriber 消费者
     * @param msg        消息体
     */
    protected abstract void deliver(RedisListener subscriber, Record<String, String> msg);

    @Override
    public void destroy() {
        if (null != container) {
            container.stop();
        }
        executor.purge();
    }

    /**
     * 创建消费者组。
     * 遍历给定的订阅者列表，为每个订阅者所指定的流创建消费者组，如果该消费者组还未在对应的流上存在的话。
     *
     * @param listeners 订阅者列表，每个订阅者包含流的键和消费者组的名称。
     */
    private void createGroup(List<RedisListener> listeners) {
        // 根据流的键对订阅者进行分组
        listeners.stream().collect(Collectors.groupingBy(RedisListener::getStreamKey)).forEach((streamKey, subs) -> {
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
