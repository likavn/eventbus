package com.github.likavn.eventbus.redis.support;

import com.github.likavn.eventbus.core.exception.EventBusException;
import io.lettuce.core.StreamMessage;
import io.lettuce.core.XReadArgs;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.apache.commons.pool2.impl.GenericObjectPool;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.function.Supplier;

/**
 * stream poll task
 *
 * @author likavn
 * @date 2024/01/01
 **/
public class StreamPollTask implements Runnable {
    private GenericObjectPool<StatefulRedisConnection<String, String>> pool;
    private final Subscriber subscriber;
    private final XReadArgs.StreamOffset<String> streamOffset;
    private final XReadArgs xReadArgs;
    private final StreamMessageListenerContainer.StreamMessageListenerContainerOptions options;
    private final Supplier<List<StreamMessage<String, String>>> supplier;
    private volatile State state = State.CREATED;
    private volatile boolean isInEventLoop = false;
    private volatile CountDownLatch awaitStart = new CountDownLatch(1);

    public StreamPollTask(GenericObjectPool<StatefulRedisConnection<String, String>> pool, Subscriber subscriber,
                          StreamMessageListenerContainer.StreamMessageListenerContainerOptions options) {
        this.subscriber = subscriber;
        this.pool = pool;
        this.options = options;
        this.streamOffset = XReadArgs.StreamOffset.lastConsumed(subscriber.getStreamKey());
        this.xReadArgs = XReadArgs.Builder.block(Duration.ofSeconds(0)).count(options.getBatchSize());
        this.supplier = getSupplier();
    }

    private Supplier<List<StreamMessage<String, String>>> getSupplier() {
        return () -> {
            try (StatefulRedisConnection<String, String> connection = pool.borrowObject()) {
                return connection.sync().xreadgroup(subscriber.getConsumer(), xReadArgs, streamOffset);
            } catch (Exception e) {
                throw new EventBusException(e);
            }
        };
    }

    private List<StreamMessage<String, String>> readMessages() {
        return supplier.get();
    }

    @Override
    public void run() {
        state = State.RUNNING;
        try {
            isInEventLoop = true;
            doLoop();
        } finally {
            isInEventLoop = false;
        }
    }

    private void doLoop() {
        do {
            try {
                // allow interruption
                Thread.sleep(0);

                List<StreamMessage<String, String>> messages = readMessages();
                if (messages.isEmpty()) {
                    continue; // 在没有新消息时继续轮询
                }
                try (StatefulRedisConnection<String, String> connection = pool.borrowObject()) {
                    RedisCommands<String, String> commands = connection.sync();
                    for (StreamMessage<String, String> message : messages) {
                        subscriber.accept(message.getStream());

                        // 手动确认消息已被处理
                        commands.xack(subscriber.getStreamKey(), subscriber.getGroup(), message.getId());
                    }
                }
            } catch (InterruptedException e) {
                cancel();
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                cancel();
                options.getErrorHandler().accept(e);
            }
        } while (isSubscriptionActive());
    }

    /**
     * @return {@literal true} if the subscription is active.
     */
    private boolean isSubscriptionActive() {
        return state == State.STARTING || state == State.RUNNING;
    }

    public boolean isActive() {
        return State.RUNNING == state || isInEventLoop;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.redis.stream.Cancelable#cancel()
     */
    public void cancel() {
        awaitStart = new CountDownLatch(1);
        state = State.CANCELLED;
    }

    enum State {
        CREATED, STARTING, RUNNING, CANCELLED;
    }
}
