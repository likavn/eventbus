package com.github.likavn.eventbus.provider.redis;

import com.github.likavn.eventbus.core.metadata.MsgType;
import com.github.likavn.eventbus.core.metadata.data.Request;
import com.github.likavn.eventbus.core.metadata.support.Subscriber;
import com.github.likavn.eventbus.core.utils.Func;
import com.github.likavn.eventbus.prop.BusProperties;
import com.github.likavn.eventbus.provider.redis.constant.RedisConstant;
import com.github.likavn.eventbus.provider.redis.support.RedisSubscriber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.PendingMessages;
import org.springframework.data.redis.connection.stream.PendingMessagesSummary;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 重新发送超时待确认消息任务
 *
 * @author likavn
 * @date 2024/1/4
 **/
@Slf4j
public class RedisPendingMsgResendTask {
    private final long pollingInterval = 35L;
    private final BusProperties busProperties;
    private final List<Subscriber> subscribers;
    private final RLock rLock;
    private final RedisMsgSender msgSender;
    private final StringRedisTemplate stringRedisTemplate;
    private final String lockKey;
    /**
     * 延时消息流key
     */
    private final String delayStreamKey;

    public RedisPendingMsgResendTask(StringRedisTemplate stringRedisTemplate,
                                     BusProperties busProperties,
                                     List<Subscriber> subscribers,
                                     RLock rLock,
                                     RedisMsgSender msgSender) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.busProperties = busProperties;
        this.subscribers = subscribers;
        this.rLock = rLock;
        this.msgSender = msgSender;
        this.lockKey = String.format(RedisConstant.NOTIFY_SUBSCRIBE_TIMEOUT_RESENDLOCK_PREFIX, busProperties.getServiceId());
        this.delayStreamKey = String.format(RedisConstant.NOTIFY_SUBSCRIBE_DELAY_PREFIX, busProperties.getServiceId());
    }

    /**
     * 一分钟执行一次,这里选择每分钟的35秒执行，是为了避免整点任务过多的问题
     */
    @Scheduled(cron = pollingInterval + " * * * * ?")
    public void messageResend() {
        try {
            // 获取锁,并锁定一定间隔时长，此处故意不释放锁，防止重复执行
            if (!rLock.getLock(lockKey, pollingInterval)) {
                return;
            }
            // 及时消息订阅
            List<RedisSubscriber> redisSubscribers = subscribers.stream().map(t
                    -> new RedisSubscriber(t, RedisConstant.NOTIFY_SUBSCRIBE_PREFIX)).collect(Collectors.toList());
            pendingMessagesResendExecute(redisSubscribers);

            // 延时的消息订阅
            Subscriber subscriberDelay = new Subscriber(busProperties.getServiceId(), null, MsgType.DELAY);
            List<RedisSubscriber> redisDelaySubscribers = Collections.singletonList(
                    new RedisSubscriber(subscriberDelay, RedisConstant.NOTIFY_SUBSCRIBE_DELAY_PREFIX));
            pendingMessagesResendExecute(redisDelaySubscribers);
        } catch (Exception e) {
            log.error("重新投递未被ack的消息失败", e);
        }
    }

    /**
     * 重新投递未被ack的消息
     */
    private void pendingMessagesResendExecute(List<RedisSubscriber> subscribers) {
        StreamOperations<String, String, String> sops = stringRedisTemplate.opsForStream();
        subscribers.forEach(subscriber -> {
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
            Map<String, Long> consumerMap = stats.getPendingMessagesPerConsumer();
            consumerMap.forEach((consumerName, msgCount) -> {
                if (msgCount <= 0) {
                    return;
                }
                log.debug("消费者：{}，一共有{}条pending消息", consumerName, msgCount);
                // 读取消费者pending队列的前N条记录，从ID=0的记录开始，一直到ID最大值
                PendingMessages pendingMessages = sops.pending(subscriber.getStreamKey(),
                        Consumer.from(subscriber.getGroup(), consumerName), Range.unbounded(), busProperties.getRedis().getPendingMessagesBatchSize());

                // 遍历所有pending消息的详情
                pendingMessages.forEach(message -> {
                    // 消息的ID
                    String recordId = message.getId().getValue();
                    // 未达到订阅消息投递超时时间 不做处理
                    long lastDelivery = message.getElapsedTimeSinceLastDelivery().getSeconds();
                    if (lastDelivery < busProperties.getRedis().getDeliverTimeout()) {
                        return;
                    }
                    // 通过streamOperations，直接读取这条pending消息，
                    List<ObjectRecord<String, String>> result =
                            sops.range(String.class, subscriber.getStreamKey(), Range.closed(recordId, recordId));
                    if (CollectionUtils.isEmpty(result)) {
                        return;
                    }
                    Request<?> request = Func.convertByJson(result.get(0).getValue());
                    // 重新投递消息
                    if (subscriber.getType().isTimely()) {
                        request.setDeliverId(subscriber.getTrigger().getDeliverId());
                        msgSender.send(request);
                    } else {
                        msgSender.send(delayStreamKey, request);
                    }
                    // 如果手动消费成功后，往消费组提交消息的ACK
                    sops.acknowledge(subscriber.getStreamKey(), subscriber.getGroup(), message.getId());
                });
            });
        });
    }
}
