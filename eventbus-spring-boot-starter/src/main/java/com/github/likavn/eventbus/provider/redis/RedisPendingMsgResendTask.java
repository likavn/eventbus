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

import com.github.likavn.eventbus.core.ListenerRegistry;
import com.github.likavn.eventbus.core.TaskRegistry;
import com.github.likavn.eventbus.core.api.MsgSender;
import com.github.likavn.eventbus.core.base.Lifecycle;
import com.github.likavn.eventbus.core.metadata.data.Request;
import com.github.likavn.eventbus.core.support.task.CronTask;
import com.github.likavn.eventbus.core.utils.Func;
import com.github.likavn.eventbus.prop.BusProperties;
import com.github.likavn.eventbus.provider.redis.support.RedisListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 重新发送超时待确认消息任务
 *
 * @author likavn
 * @date 2024/1/4
 **/
@Slf4j
public class RedisPendingMsgResendTask implements Runnable, Lifecycle {
    private static final long POLLING_INTERVAL = 35L;
    private static final String CRON = POLLING_INTERVAL + " * * * * ?";
    private final BusProperties busProperties;
    private final RLock rLock;
    private final MsgSender msgSender;
    private final StringRedisTemplate stringRedisTemplate;
    private final List<RedisListener> redisSubscribers;
    private CronTask task;
    private final TaskRegistry taskRegistry;

    public RedisPendingMsgResendTask(StringRedisTemplate stringRedisTemplate, TaskRegistry taskRegistry,
                                     BusProperties busProperties, ListenerRegistry registry,
                                     RLock rLock,
                                     MsgSender msgSender) {
        // 一分钟执行一次,这里选择每分钟的35秒执行，是为了避免整点任务过多的问题
        this.stringRedisTemplate = stringRedisTemplate;
        this.taskRegistry = taskRegistry;
        this.busProperties = busProperties;
        this.rLock = rLock;
        this.msgSender = msgSender;
        // 及时消息订阅
        this.redisSubscribers = RedisListener.getAllListeners(registry);
    }

    @Override
    public void register() {
        task = CronTask.create(this.getClass().getName(), CRON, this);
        taskRegistry.createTask(task);
    }

    @Override
    public void run() {
        this.redisSubscribers.forEach(subscriber -> {
            String lockKey = subscriber.getStreamKey() + ".pendingMsgResendLock." + subscriber.getGroup();
            // 获取锁,并锁定一定间隔时长，此处故意不释放锁，防止重复执行
            boolean lock = false;
            try {
                lock = rLock.getLock(lockKey, POLLING_INTERVAL * 2);
                if (!lock) {
                    return;
                }
                pendingMessagesResendExecute(subscriber);
            } catch (Exception e) {
                log.error("pending消息重发异常", e);
            } finally {
                if (lock) {
                    try {
                        rLock.releaseLock(lockKey);
                    } catch (Exception e) {
                        log.error("pending.releaseLock", e);
                    }
                }
            }
        });
    }

    /**
     * 重新发送pending消息
     *
     * @param subscriber 消费者
     */
    private void pendingMessagesResendExecute(RedisListener subscriber) {
        StreamOperations<String, String, String> sops = stringRedisTemplate.opsForStream();
        // 获取my_group中的pending消息信息
        PendingMessagesSummary stats;
        try {
            stats = sops.pending(subscriber.getStreamKey(), subscriber.getGroup());
        } catch (RedisSystemException e) {
            if (("" + e.getMessage()).contains("No such key")) {
                return;
            }
            log.error(e.getMessage());
            return;
        }
        if (null == stats) {
            return;
        }
        // 所有pending消息的数量
        long totalMsgNum = stats.getTotalPendingMessages();
        if (totalMsgNum <= 0) {
            return;
        }

        log.debug("消费组：{}，一共有{}条pending消息...", subscriber.getGroup(), totalMsgNum);
        // 每个消费者的pending消息数量
        stats.getPendingMessagesPerConsumer().forEach((consumerName, msgCount) -> {
            if (msgCount <= 0) {
                return;
            }
            log.debug("消费者：{}，一共有{}条pending消息", consumerName, msgCount);
            int pendingMessagesBatchSize = busProperties.getRedis().getPendingMessagesBatchSize();
            do {
                // 读取消费者pending队列的前N条记录，从ID=0的记录开始，一直到ID最大值
                PendingMessages pendingMessages = sops.pending(subscriber.getStreamKey(),
                        Consumer.from(subscriber.getGroup(), consumerName), Range.unbounded(), pendingMessagesBatchSize);
                if (pendingMessages.isEmpty()) {
                    return;
                }
                pushMessage(subscriber, pendingMessages);
            } while ((msgCount = msgCount - pendingMessagesBatchSize) > 0);
        });
    }

    /**
     * 从pending队列中读取消息
     */
    public void pushMessage(RedisListener listener, PendingMessages pendingMessages) {
        // 遍历所有pending消息的详情
        pendingMessages.get().parallel().forEach(message -> {
            // 消息的ID
            String recordId = message.getId().getValue();
            // 未达到订阅消息投递超时时间 不做处理
            long lastDelivery = message.getElapsedTimeSinceLastDelivery().getSeconds();
            if (lastDelivery < busProperties.getRedis().getDeliverTimeout()) {
                return;
            }
            // 通过streamOperations，直接读取这条pending消息，
            List<ObjectRecord<String, String>> result = stringRedisTemplate
                    .opsForStream().range(String.class, listener.getStreamKey(), Range.closed(recordId, recordId));
            if (CollectionUtils.isEmpty(result)) {
                acknowledge(listener, message.getId());
                return;
            }
            Request<?> request = Func.convertByJson(result.get(0).getValue());
            request.setDeliverCount(request.getDeliverCount() + 1);
            // 重新投递消息
            request.setDeliverId(listener.getDeliverId());
            request.setDelayTime(1L);
            request.setRetry(true);
            msgSender.sendDelayMessage(request);
            acknowledge(listener, message.getId());
            // 重试队列删除消息
            if (listener.isRetry()) {
                stringRedisTemplate.opsForStream().delete(listener.getStreamKey(), message.getId());
            }
        });
    }

    /**
     * 确认消费
     *
     * @param subscriber 消费者
     * @param recordId   消息ID
     */
    private void acknowledge(RedisListener subscriber, RecordId recordId) {
        stringRedisTemplate.opsForStream().acknowledge(subscriber.getStreamKey(), subscriber.getGroup(), recordId);
    }

    @Override
    public void destroy() {
        taskRegistry.removeTask(task);
    }
}
