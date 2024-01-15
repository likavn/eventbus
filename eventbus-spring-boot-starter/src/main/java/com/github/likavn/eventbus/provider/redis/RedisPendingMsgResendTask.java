package com.github.likavn.eventbus.provider.redis;

import com.github.likavn.eventbus.core.base.NetLifecycle;
import com.github.likavn.eventbus.core.metadata.MsgType;
import com.github.likavn.eventbus.core.metadata.data.Request;
import com.github.likavn.eventbus.core.metadata.support.Subscriber;
import com.github.likavn.eventbus.core.utils.Func;
import com.github.likavn.eventbus.prop.BusProperties;
import com.github.likavn.eventbus.provider.redis.constant.RedisConstant;
import com.github.likavn.eventbus.provider.redis.support.RedisSubscriber;
import com.github.likavn.eventbus.schedule.ScheduledTaskRegistry;
import com.github.likavn.eventbus.schedule.Task;
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
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 重新发送超时待确认消息任务
 *
 * @author likavn
 * @date 2024/1/4
 **/
@Slf4j
public class RedisPendingMsgResendTask implements NetLifecycle {
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
    private final ScheduledTaskRegistry scheduledTaskRegistry;
    private final List<RedisSubscriber> redisSubscribers;
    private Task task;


    public RedisPendingMsgResendTask(StringRedisTemplate stringRedisTemplate, ScheduledTaskRegistry scheduledTaskRegistry,
                                     BusProperties busProperties, List<Subscriber> subscribers,
                                     RLock rLock,
                                     RedisMsgSender msgSender) {
        // 一分钟执行一次,这里选择每分钟的35秒执行，是为了避免整点任务过多的问题
        this.stringRedisTemplate = stringRedisTemplate;
        this.scheduledTaskRegistry = scheduledTaskRegistry;
        this.busProperties = busProperties;
        this.rLock = rLock;
        this.msgSender = msgSender;
        this.delayStreamKey = String.format(RedisConstant.NOTIFY_SUBSCRIBE_DELAY_PREFIX, busProperties.getServiceId());
        // 及时消息订阅
        this.redisSubscribers = subscribers.stream().map(t
                -> new RedisSubscriber(t, RedisConstant.NOTIFY_SUBSCRIBE_PREFIX)).collect(Collectors.toList());

        // 延时的消息订阅
        Subscriber subscriberDelay = new Subscriber(busProperties.getServiceId(), null, MsgType.DELAY);
        this.redisSubscribers.add(new RedisSubscriber(subscriberDelay, RedisConstant.NOTIFY_SUBSCRIBE_DELAY_PREFIX));
    }

    @Override
    public void register() {
        if (null == this.task) {
            this.task = new Task(this.getClass().getName(), CRON, () -> pendingMessagesResendExecute(this.redisSubscribers));
            scheduledTaskRegistry.createTask(this.task);
            return;
        }
        this.scheduledTaskRegistry.restart(task);
    }

    /**
     * 重新投递未被ack的消息
     */
    public void pendingMessagesResendExecute(List<RedisSubscriber> subscribers) {
        StreamOperations<String, String, String> sops = stringRedisTemplate.opsForStream();
        subscribers.forEach(subscriber -> {
            String lockKey = String.format(RedisConstant.PENDING_MSG_LOCK_PREFIX, subscriber.getStreamKey(), subscriber.getGroup());
            // 获取锁,并锁定一定间隔时长，此处故意不释放锁，防止重复执行
            if (!rLock.getLock(lockKey, POLLING_INTERVAL)) {
                return;
            }
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
                Integer pendingMessagesBatchSize = busProperties.getRedis().getPendingMessagesBatchSize();
                long count = msgCount / pendingMessagesBatchSize;
                while (count-- >= 0) {
                    // 读取消费者pending队列的前N条记录，从ID=0的记录开始，一直到ID最大值
                    PendingMessages pendingMessages = sops.pending(subscriber.getStreamKey(),
                            Consumer.from(subscriber.getGroup(), consumerName), Range.unbounded(), pendingMessagesBatchSize);
                    if (pendingMessages.isEmpty()) {
                        return;
                    }
                    pushMessage(subscriber, pendingMessages);
                }
            });
        });
    }

    /**
     * 从pending队列中读取消息
     */
    public void pushMessage(RedisSubscriber subscriber, PendingMessages pendingMessages) {
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
            List<ObjectRecord<String, String>> result = stringRedisTemplate
                    .opsForStream().range(String.class, subscriber.getStreamKey(), Range.closed(recordId, recordId));
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
            stringRedisTemplate.opsForStream().acknowledge(subscriber.getStreamKey(), subscriber.getGroup(), message.getId());
        });
    }

    @Override
    public void destroy() {
        this.scheduledTaskRegistry.pause(task);
    }
}
