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
package com.github.likavn.eventbus.provider.redis;

import com.github.likavn.eventbus.core.DeliveryBus;
import com.github.likavn.eventbus.core.constant.BusConstant;
import com.github.likavn.eventbus.core.metadata.MsgType;
import com.github.likavn.eventbus.core.metadata.support.Subscriber;
import com.github.likavn.eventbus.prop.BusProperties;
import com.github.likavn.eventbus.provider.redis.constant.RedisConstant;
import com.github.likavn.eventbus.provider.redis.support.AbstractStreamListenerContainer;
import com.github.likavn.eventbus.provider.redis.support.RedisSubscriber;
import com.github.likavn.eventbus.schedule.PeriodTask;
import com.github.likavn.eventbus.schedule.ScheduledTaskRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.Record;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 延时消息处理类
 *
 * @author likavn
 * @since 2023/01/01
 */
@Slf4j
public class RedisMsgDelayListener extends AbstractStreamListenerContainer {
    /**
     * 轮询时间间隔，单位：毫秒
     */
    private static final long POLL_MILLIS = 200L;
    /**
     * 最大轮询时间间隔，单位：毫秒
     */
    private static final long MAX_POLL_MILLIS = 1000L * 5;
    /**
     * 最大消息推送数量，默认10万条
     */
    private static final long MAX_PUSH_COUNT = 10000L * 10;

    private final StringRedisTemplate stringRedisTemplate;
    private final RLock rLock;
    private final DeliveryBus deliveryBus;
    private final DefaultRedisScript<Long> pushMsgStreamRedisScript;
    /**
     * 延时消息key,zset
     */
    private final String delayZetKey;
    /**
     * 轮询锁
     */
    private final String pollLockKey;
    /**
     * 延时消息流key
     */
    private final String delayStreamKey;
    private final ScheduledTaskRegistry scheduledTaskRegistry;
    private PeriodTask task;

    public RedisMsgDelayListener(StringRedisTemplate stringRedisTemplate,
                                 ScheduledTaskRegistry scheduledTaskRegistry,
                                 BusProperties busProperties,
                                 DefaultRedisScript<Long> pushMsgStreamRedisScript, RLock rLock, DeliveryBus deliveryBus) {
        super(stringRedisTemplate, busProperties, BusConstant.DELAY_MSG_THREAD_NAME);
        this.stringRedisTemplate = stringRedisTemplate;
        this.scheduledTaskRegistry = scheduledTaskRegistry;
        this.pushMsgStreamRedisScript = pushMsgStreamRedisScript;
        this.rLock = rLock;
        this.deliveryBus = deliveryBus;
        this.delayZetKey = String.format(RedisConstant.BUS_DELAY_PREFIX, busProperties.getServiceId());
        this.pollLockKey = String.format(RedisConstant.BUS_DELAY_LOCK_PREFIX, busProperties.getServiceId());
        this.delayStreamKey = String.format(RedisConstant.BUS_DELAY_SUBSCRIBE_PREFIX, busProperties.getServiceId());
    }

    @Override
    protected List<RedisSubscriber> getSubscribers() {
        RedisSubscriber subscriber = new RedisSubscriber(new Subscriber(
                busProperties.getServiceId(), null, MsgType.DELAY), RedisConstant.BUS_DELAY_SUBSCRIBE_PREFIX);
        return Collections.singletonList(subscriber);
    }

    @Override
    protected void deliver(RedisSubscriber subscriber, Record<String, String> msg) {
        deliveryBus.deliverDelay(msg.getValue());
        stringRedisTemplate.opsForStream().acknowledge(subscriber.getStreamKey(), subscriber.getGroup(), msg.getId());
    }

    @Override
    public synchronized void register() {
        super.register();
        if (null == this.task) {
            this.task = new PeriodTask(this.getClass().getName(), POLL_MILLIS, this::pollTask);
            scheduledTaskRegistry.createTask(this.task);
            return;
        }

        // 任务重启
        scheduledTaskRegistry.restart(task);
    }

    /**
     * 循环获取延时队列到期消息
     */
    private void pollTask() {
        boolean lock = false;
        try {
            lock = rLock.getLock(pollLockKey);
            if (!lock) {
                return;
            }
            Long nextCurrentTimeMillis = stringRedisTemplate.execute(pushMsgStreamRedisScript,
                    Arrays.asList(delayZetKey, delayStreamKey),
                    // 到当前时间之前的消息 + 推送数量
                    String.valueOf(System.currentTimeMillis()), String.valueOf(MAX_PUSH_COUNT));
            if (null == nextCurrentTimeMillis) {
                nextCurrentTimeMillis = System.currentTimeMillis() + MAX_POLL_MILLIS;
            }
            setNextTriggerTimeMillis(nextCurrentTimeMillis);
        } finally {
            if (lock) {
                rLock.releaseLock(pollLockKey);
            }
        }
    }

    /**
     * 重置轮询时间
     */
    public void setNextTriggerTimeMillis(long timeMillis) {
        this.task.setNextTriggerTimeMillis(timeMillis);
    }

    @Override
    public void destroy() {
        super.destroy();
        // 任务重启
        if (null != task) {
            scheduledTaskRegistry.pause(task);
        }
    }
}