package com.github.likavn.eventbus.redis.support;

import io.lettuce.core.api.StatefulRedisConnection;
import lombok.Builder;
import lombok.Getter;
import org.apache.commons.pool2.impl.GenericObjectPool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * stream 监听容器
 *
 * @author likavn
 * @date 2024/01/01
 **/
public class StreamMessageListenerContainer {
    /**
     * 任务列表
     */
    private final List<StreamPollTask> tasks = new ArrayList<>();
    private StreamMessageListenerContainerOptions options;
    private GenericObjectPool<StatefulRedisConnection<String, String>> pool;
    private volatile boolean running = false;

    private StreamMessageListenerContainer(StreamMessageListenerContainerOptions options) {
        this.options = options;
    }

    public StreamMessageListenerContainer create(GenericObjectPool<StatefulRedisConnection<String, String>> pool,
                                                 StreamMessageListenerContainerOptions options) {
        return new StreamMessageListenerContainer(options);
    }

    public void receive(Subscriber subscriber) {
        tasks.add(new StreamPollTask(pool, subscriber, options));
    }

    /**
     * 启动
     */
    public synchronized void start() {
        if (running) {
            return;
        }
        running = true;
        tasks.forEach(StreamPollTask::run);
    }

    /**
     * 停止
     */
    public void stop() {
        if (!running) {
            return;
        }
        running = false;
        tasks.forEach(StreamPollTask::cancel);
    }

    @Builder
    @Getter
    public static class StreamMessageListenerContainerOptions {
        private Executors executors;
        private Consumer<Throwable> errorHandler;
        private Long batchSize;
    }

}
