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

import com.github.likavn.eventbus.core.metadata.data.Request;
import com.github.likavn.eventbus.core.metadata.support.Subscriber;
import com.github.likavn.eventbus.core.utils.Func;
import com.github.likavn.eventbus.prop.BusProperties;
import com.github.likavn.eventbus.provider.redis.constant.RedisConstant;
import com.github.likavn.eventbus.provider.redis.support.RedisSubscriber;
import com.github.likavn.eventbus.schedule.CronTask;
import com.github.likavn.eventbus.schedule.ScheduledTaskRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.PendingMessages;
import org.springframework.data.redis.connection.stream.PendingMessagesSummary;
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
public class RedisPendingMsgResendTask extends CronTask {
    private static final long POLLING_INTERVAL = 35L;
    private static final String CRON = POLLING_INTERVAL + " * * * * ?";
    private final BusProperties busProperties;
    private final RLock rLock;
    private final RedisMsgSender msgSender;
    private final StringRedisTemplate stringRedisTemplate;
    /**
     * 延时消息流key
     */
    private final String delayStreamKey;
    private final List<RedisSubscriber> redisSubscribers;

    public RedisPendingMsgResendTask(StringRedisTemplate stringRedisTemplate, ScheduledTaskRegistry taskRegistry,
                                     BusProperties busProperties, List<Subscriber> subscribers,
                                     RLock rLock,
                                     RedisMsgSender msgSender) {
        super(taskRegistry, RedisPendingMsgResendTask.class.getName(), CRON);
        // 一分钟执行一次,这里选择每分钟的35秒执行，是为了避免整点任务过多的问题
        this.stringRedisTemplate = stringRedisTemplate;
        this.busProperties = busProperties;
        this.rLock = rLock;
        this.msgSender = msgSender;
        this.delayStreamKey = String.format(RedisConstant.BUS_DELAY_SUBSCRIBE_PREFIX, busProperties.getServiceId());
        // 及时消息订阅
        this.redisSubscribers = RedisSubscriber.fullRedisSubscriber(subscribers, busProperties.getServiceId());
    }

    @Override
    public void run() {
        this.redisSubscribers.forEach(subscriber -> {
            String lockKey = subscriber.getStreamKey() + ".pendingMsgResendLock." + subscriber.getGroup();
            // 获取锁,并锁定一定间隔时长，此处故意不释放锁，防止重复执行
            boolean lock = false;
            try {
                lock = rLock.getLock(lockKey, POLLING_INTERVAL);
                if (!lock) {
                    return;
                }
                pendingMessagesResendExecute(subscriber);
            } catch (Exception e) {
                log.error("pending消息重发异常", e);
            } finally {
                if (lock) {
                    rLock.releaseLock(lockKey);
                }
            }
        });
    }

    private void pendingMessagesResendExecute(RedisSubscriber subscriber) {
        StreamOperations<String, String, String> sops = stringRedisTemplate.opsForStream();
        // 获取my_group中的pending消息信息
        PendingMessagesSummary stats = sops.pending(subscriber.getStreamKey(), subscriber.getGroup());
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
            } while ((msgCount -= pendingMessagesBatchSize) > 0);
        });
    }

    /**
     * 从pending队列中读取消息
     */
    public void pushMessage(RedisSubscriber subscriber, PendingMessages pendingMessages) {
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
                    .opsForStream().range(String.class, subscriber.getStreamKey(), Range.closed(recordId, recordId));
            if (CollectionUtils.isEmpty(result)) {
                return;
            }
            Request<?> request = Func.convertByJson(result.get(0).getValue());
            request.setDeliverCount(request.getDeliverCount() + 1);
            // 重新投递消息
            if (subscriber.getType().isTimely()) {
                request.setDeliverId(subscriber.getTrigger().getDeliverId());
                msgSender.toSend(request);
            } else {
                msgSender.toSend(delayStreamKey, request);
            }
            // 如果手动消费成功后，往消费组提交消息的ACK
            stringRedisTemplate.opsForStream().acknowledge(subscriber.getStreamKey(), subscriber.getGroup(), message.getId());
        });
    }
}
