package com.github.likavn.eventbus.provider.redis;

import com.github.likavn.eventbus.core.DeliveryBus;
import com.github.likavn.eventbus.core.constant.BusConstant;
import com.github.likavn.eventbus.core.utils.Func;
import com.github.likavn.eventbus.prop.BusProperties;
import com.github.likavn.eventbus.provider.redis.constant.RedisConstant;
import com.github.likavn.eventbus.provider.redis.support.AbstractDefaultStreamContainer;
import com.github.likavn.eventbus.provider.redis.support.XStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.util.Arrays;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 延时消息处理类
 *
 * @author likavn
 * @since 2023/01/01
 */
@Slf4j
public class RedisMsgDelayListener extends AbstractDefaultStreamContainer {
    /**
     * 轮询时间间隔，单位：毫秒
     */
    private static final long POLL_MILLIS = 500L;
    /**
     * 最大消息推送数量，默认10万条
     */
    private static final long MAX_PUSH_COUNT = 10000L * 10;

    private final StringRedisTemplate stringRedisTemplate;
    private final RLock rLock;
    private final DefaultRedisScript<Void> pushMsgStreamRedisScript;
    private final DeliveryBus deliveryBus;
    /**
     * 延时消息key,zset
     */
    private final String delayKey;
    /**
     * 轮询锁
     */
    private final String pollLockKey;
    /**
     * 延时消息流key
     */
    private final String delayStreamKey;
    private ScheduledThreadPoolExecutor scheduler;

    public RedisMsgDelayListener(StringRedisTemplate stringRedisTemplate,
                                 BusProperties busProperties, DefaultRedisScript<Void> pushMsgStreamRedisScript, RLock rLock, DeliveryBus deliveryBus) {
        super(stringRedisTemplate, busProperties);
        this.stringRedisTemplate = stringRedisTemplate;
        this.pushMsgStreamRedisScript = pushMsgStreamRedisScript;
        this.rLock = rLock;
        this.delayKey = String.format(RedisConstant.NOTIFY_DELAY_PREFIX, busProperties.getServiceId());
        this.pollLockKey = String.format(RedisConstant.NOTIFY_DELAY_LOCK_PREFIX, busProperties.getServiceId());
        this.delayStreamKey = String.format(RedisConstant.NOTIFY_SUBSCRIBE_DELAY_PREFIX, busProperties.getServiceId());
        this.deliveryBus = deliveryBus;
    }

    @Override
    public synchronized void register() {
        super.register();
        if (null == scheduler) {
            scheduler = new ScheduledThreadPoolExecutor(1, new CustomizableThreadFactory(BusConstant.DELAY_MSG_THREAD_NAME));
        }
        // 定时轮询获取延时队列到期消息
        scheduler.scheduleWithFixedDelay(this::pollTask, 0, POLL_MILLIS, TimeUnit.MILLISECONDS);
    }

    /**
     * 循环获取延时队列到期消息
     */
    private void pollTask() {
        if (!rLock.getLock(pollLockKey)) {
            return;
        }
        stringRedisTemplate.execute(pushMsgStreamRedisScript, Arrays.asList(delayKey, delayStreamKey),
                // 到当前时间之前的消息 + 推送数量
                String.valueOf(System.currentTimeMillis()), String.valueOf(MAX_PUSH_COUNT));
    }

    @Override
    protected void addReceives(StreamMessageListenerContainer<String, ObjectRecord<String, String>> listenerContainer) {
        String groupName = busProperties.getServiceId();
        // 初始化组
        XStream.addConsumerGroup(stringRedisTemplate, delayStreamKey, groupName);

        int num = 1;
        while (num++ <= busProperties.getConsumerNum()) {
            // 使用监听容器对象开始监听消费（使用的是手动确认方式）
            listenerContainer.receive(Consumer.from(groupName, Func.getHostName() + "-" + num),
                    StreamOffset.create(delayStreamKey, ReadOffset.lastConsumed()),
                    msg -> {
                        deliveryBus.deliverDelay(msg.getValue());
                        stringRedisTemplate.opsForStream().acknowledge(delayStreamKey, groupName, msg.getId());
                    });
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        if (null != scheduler) {
            scheduler.shutdown();
            scheduler = null;
        }
    }
}
