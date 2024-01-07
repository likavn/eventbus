package com.github.likavn.eventbus.provider.redis;

import com.github.likavn.eventbus.core.api.MsgSender;
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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.CollectionUtils;

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
    private final BusProperties.RedisProperties redisProperties;
    private final List<RedisSubscriber> subscribers;
    private final RLock rLock;
    private final MsgSender msgSender;
    private final RedisTemplate<String, String> redisTemplate;
    private final String lockKey;

    public RedisPendingMsgResendTask(BusProperties busProperties,
                                     List<Subscriber> subscribers,
                                     RLock rLock,
                                     MsgSender msgSender,
                                     RedisTemplate<String, String> redisTemplate) {
        this.redisProperties = busProperties.getRedis();
        this.subscribers = subscribers.stream().map(RedisSubscriber::new).collect(Collectors.toList());
        this.redisTemplate = redisTemplate;
        this.rLock = rLock;
        this.msgSender = msgSender;
        this.lockKey = String.format(RedisConstant.NOTIFY_SUBSCRIBE_TIMEOUT_RESENDLOCK_PREFIX, busProperties.getServiceId());
    }

    /**
     * 一分钟执行一次,这里选择每分钟的35秒执行，是为了避免整点任务过多的问题
     */
    @Scheduled(cron = pollingInterval + " * * * * ?")
    public void messageResend() {
        try {
            // 获取锁,并锁定一定间隔时长，此处故意不释放锁，防止重复执行
            boolean lock = rLock.getLock(lockKey, pollingInterval);
            if (!lock) {
                return;
            }
            pendingMessagesResendExecute();
        } catch (Exception e) {
            log.error("重新投递未被ack的消息失败", e);
        }
    }

    /**
     * 重新投递未被ack的消息
     */
    private void pendingMessagesResendExecute() {
        StreamOperations<String, String, String> sops = redisTemplate.opsForStream();
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
                        Consumer.from(subscriber.getGroup(), consumerName), Range.unbounded(), redisProperties.getPendingMessagesBatchSize());

                // 遍历所有pending消息的详情
                pendingMessages.forEach(message -> {
                    // 消息的ID
                    String recordId = message.getId().getValue();
                    // 未达到订阅消息投递超时时间 不做处理
                    long lastDelivery = message.getElapsedTimeSinceLastDelivery().getSeconds();
                    if (lastDelivery < redisProperties.getDeliverTimeout()) {
                        return;
                    }
                    // 通过streamOperations，直接读取这条pending消息，
                    List<ObjectRecord<String, String>> result =
                            sops.range(String.class, subscriber.getStreamKey(), Range.closed(recordId, recordId));
                    if (CollectionUtils.isEmpty(result)) {
                        return;
                    }

                    // 重新投递消息
                    Request<?> request = Func.convertByJson(result.get(0).getValue());
                    request.setDeliverId(subscriber.getTrigger().getDeliverId());
                    msgSender.send(request);
                    // 如果手动消费成功后，往消费组提交消息的ACK
                    sops.acknowledge(subscriber.getStreamKey(), subscriber.getGroup(), message.getId());
                });
            });
        });
    }
}
