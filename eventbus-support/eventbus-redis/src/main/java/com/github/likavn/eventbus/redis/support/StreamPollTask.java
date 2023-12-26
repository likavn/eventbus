package com.github.likavn.eventbus.redis.support;

import io.lettuce.core.StreamMessage;
import io.lettuce.core.XReadArgs;
import io.lettuce.core.api.sync.RedisCommands;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * stream poll task
 *
 * @author likavn
 * @date 2023/10/19
 **/
public class StreamPollTask implements Runnable {
    private final RedisCommands<String, String> commands;
    private final Subscriber subscriber;
    private final XReadArgs.StreamOffset<String> streamOffset;
    private final XReadArgs xReadArgs;
    private final Long msgCount;
    private final Supplier<List<StreamMessage<String, String>>> supplier;
    private volatile State state = State.CREATED;
    private volatile boolean isInEventLoop = false;
    private volatile CountDownLatch awaitStart = new CountDownLatch(1);
    private final Consumer<Throwable> errorHandler;

    public StreamPollTask(RedisCommands<String, String> commands, Subscriber subscriber, Long msgCount, Consumer<Throwable> errorHandler) {
        this.commands = commands;
        this.subscriber = subscriber;
        this.msgCount = msgCount;
        this.errorHandler = errorHandler;
        this.streamOffset = XReadArgs.StreamOffset.lastConsumed(subscriber.getStreamKey());
        this.xReadArgs = XReadArgs.Builder.block(Duration.ofSeconds(0));
        this.supplier = getSupplier();
    }

    private Supplier<List<StreamMessage<String, String>>> getSupplier() {
        return () -> commands.xreadgroup(subscriber.getConsumer(), xReadArgs, streamOffset);
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

                for (StreamMessage<String, String> message : messages) {
                    subscriber.accept(message.getStream());

                    // 手动确认消息已被处理
                    commands.xack(subscriber.getStreamKey(), subscriber.getGroup(), message.getId());
                }
            } catch (InterruptedException e) {
                cancel();
                Thread.currentThread().interrupt();
            } catch (RuntimeException e) {
                cancel();
                errorHandler.accept(e);
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
