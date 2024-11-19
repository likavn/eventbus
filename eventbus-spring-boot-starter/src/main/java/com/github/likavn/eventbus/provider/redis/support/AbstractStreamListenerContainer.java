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

import com.github.likavn.eventbus.core.base.AcquireListeners;
import com.github.likavn.eventbus.core.base.Lifecycle;
import com.github.likavn.eventbus.core.constant.BusConstant;
import com.github.likavn.eventbus.core.utils.Func;
import com.github.likavn.eventbus.core.utils.GroupedThreadPoolExecutor;
import com.github.likavn.eventbus.core.utils.NamedThreadFactory;
import com.github.likavn.eventbus.core.utils.PollThreadPoolExecutor;
import com.github.likavn.eventbus.prop.BusProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Record;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer.StreamMessageListenerContainerOptions;
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
public abstract class AbstractStreamListenerContainer implements AcquireListeners<RedisListener>, Lifecycle {
    protected final StringRedisTemplate redisTemplate;
    protected final BusProperties config;
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
        appStartup();
    }

    /**
     * 应用启动时执行的Redis监听器初始化方法
     * 初始化包括创建执行器、配置监听容器等操作
     */
    private void appStartup() {
        // 获取配置的Redis监听器列表
        List<? extends RedisListener> listeners = getListeners();
        // 如果监听器列表为空，则直接返回
        if (Func.isEmpty(listeners)) {
            return;
        }
        // 根据监听器列表和阻塞配置创建执行器
        Object[] executors = createExecutor();
        // 保存线程池执行器，用于后续的配置
        ThreadPoolExecutor pollExecutor = (PollThreadPoolExecutor) executors[0];
        // 开始构建监听容器的配置对象
        StreamMessageListenerContainerOptions<String, ObjectRecord<String, String>> options
                = StreamMessageListenerContainerOptions.builder()
                .executor(pollExecutor)
                // 设置一次性最多拉取的消息数量
                .batchSize(config.getMsgBatchSize())
                // 设置消息消费异常的处理程序
                .errorHandler(t -> log.error("[Eventbus error] ", t))
                // 设置轮询超时时间，如果设置为0，则表示不超时
                .pollTimeout(Duration.ofMillis(config.getRedis().getPollBlockMillis()))
                // 设置序列化器
                .serializer(new StringRedisSerializer())
                // 设置目标类型为String
                .targetType(String.class)
                .build();
        // 根据配置对象创建监听容器
        RedisConnectionFactory connectionFactory = redisTemplate.getConnectionFactory();
        Assert.notNull(connectionFactory, "RedisConnectionFactory must not be null!");
        container = new XDefaultStreamMessageListenerContainer<>(connectionFactory, options, (GroupedThreadPoolExecutor) executors[1]);
        // 为监听容器添加消费者
        createConsumer(container, listeners);
        // 启动监听容器，开始监听Redis消息
        container.start();
    }

    /**
     * 创建线程池
     *
     * @return 线程池
     */
    private Object[] createExecutor() {
        BusProperties.RedisProperties redis = config.getRedis();
        // 根据配置创建不同的线程池
        PollThreadPoolExecutor executor = new PollThreadPoolExecutor(redis.getPollThreadPoolSize(), redis.getPollThreadPoolSize(), 1,
                TimeUnit.MINUTES, new LinkedBlockingDeque<>(1), new NamedThreadFactory(this.getClass().getSimpleName() + ".poll-"));
        // 分发消息的线程池
        GroupedThreadPoolExecutor deliverExecutor = new GroupedThreadPoolExecutor(redis.getDeliverGroupThreadPoolSize(), redis.getDeliverGroupThreadKeepAliveTime(),
                new NamedThreadFactory(this.getClass().getSimpleName() + ".deliver-"));
        return new Object[]{executor, deliverExecutor};
    }

    /**
     * 创建消费者并将其注册到消息监听器容器中
     * 此方法首先获取当前主机地址，然后为每个Redis监听器创建一个消费者，并配置相应的流偏移量
     * 如果容器是特定类型（XDefaultStreamMessageListenerContainer），会使用一种特定的方式注册消费者
     * 否则，将使用通用方式使容器接收消息
     *
     * @param container 消息监听器容器，用于管理消费者和处理消息
     * @param listeners 一个或多个Redis事件监听器，每个监听器代表一个消息流的监听点
     */
    private void createConsumer(StreamMessageListenerContainer<String, ObjectRecord<String, String>> container, List<? extends RedisListener> listeners) {
        // 获取当前设备的主机地址，用于标识消息的消费点
        String hostAddress = Func.getHostAddress();
        // 初始化监听器组，这一步可能涉及到创建或更新Redis中的消费者组
        createGroup(listeners);
        // 遍历每个Redis监听器，为每个流创建并配置消费者
        for (RedisListener listener : listeners) {
            // 根据监听器的并发级别启动消费者线程
            Func.pollRun(listener.isRetry() ? listener.getRetryConcurrency() : listener.getConcurrency(), () -> {
                // 从监听器的组信息和当前主机地址创建一个消费者实例
                Consumer consumer = Consumer.from(listener.getGroup(), hostAddress);
                // 创建流偏移量，指定从最后已消费的消息之后开始读取
                StreamOffset<String> offset = StreamOffset.create(listener.getStreamKey(), ReadOffset.lastConsumed());
                // 如果容器是XDefaultStreamMessageListenerContainer类型，则使用特定方法注册消费者
                if (container instanceof XDefaultStreamMessageListenerContainer) {
                    // 使用构建者模式配置消费者的读取请求，并注册到容器中
                    // 指定消费策略，包括不自动确认消息和处理消息的回调函数
                    ((XDefaultStreamMessageListenerContainer<String, ObjectRecord<String, String>>) container).register(StreamMessageListenerContainer
                                    .StreamReadRequest.builder(offset).consumer(consumer).autoAcknowledge(false).build(),
                            msg -> deliverMsg(listener, msg), listener);
                    return;
                }
                // 如果容器不是特定类型，则通过接收方法使容器接收消息
                // 这种方式适用于更广泛的容器类型
                container.receive(consumer, offset, msg -> deliverMsg(listener, msg));
            });
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
            if (listener.isRetry()) {
                redisTemplate.opsForStream().delete(listener.getStreamKey(), msg.getId());
            }
        } catch (Exception e) {
            log.error("[Eventbus error] ", e);
        } finally {
            // 恢复线程名称
            Thread.currentThread().setName(oldName);
        }
    }

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
    }

    /**
     * 创建消费者组。
     * 遍历给定的订阅者列表，为每个订阅者所指定的流创建消费者组，如果该消费者组还未在对应的流上存在的话。
     *
     * @param listeners 订阅者列表，每个订阅者包含流的键和消费者组的名称。
     */
    private void createGroup(List<? extends RedisListener> listeners) {
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
                subs.forEach(t -> {
                    try {
                        redisTemplate.opsForStream().createGroup(t.getStreamKey(), t.getGroup());
                    } catch (Exception e) {
                        if (!e.getMessage().contains("Group name already exists")) {
                            throw e;
                        }
                    }
                });
            }
        });
    }
}
