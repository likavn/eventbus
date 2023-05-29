package com.github.likavn.notify.provider.redis;

import com.github.likavn.notify.base.MsgListenerInit;
import com.github.likavn.notify.domain.SubMsgConsumer;
import com.github.likavn.notify.prop.NotifyProperties;
import com.github.likavn.notify.provider.redis.constant.RedisConstant;
import com.github.likavn.notify.provider.redis.domain.RedisSubMsgConsumer;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.util.CollectionUtils;

import java.net.InetAddress;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * redis消息监听器
 *
 * @author likavn
 * @since 2023/01/01
 **/
@Slf4j
public class RedisSubscribeMsgListener implements MsgListenerInit, DisposableBean {
    private final NotifyProperties.Redis redisConfig;

    private final List<RedisSubMsgConsumer> subMsgConsumers;

    private final RedisConnectionFactory redisConnectionFactory;

    private final RedisTemplate<String, String> redisTemplate;

    private final RLock rLock;

    private StreamMessageListenerContainer<String, ObjectRecord<String, String>> listenerContainer;

    public RedisSubscribeMsgListener(RedisConnectionFactory redisConnectionFactory,
                                     NotifyProperties notifyProperties,
                                     RedisTemplate<String, String> redisTemplate,
                                     List<SubMsgConsumer> subMsgConsumers, RLock rLock) {
        this.redisConnectionFactory = redisConnectionFactory;
        this.redisConfig = notifyProperties.getRedis();
        this.subMsgConsumers = subMsgConsumers.stream().map(RedisSubMsgConsumer::new).collect(Collectors.toList());
        this.redisTemplate = redisTemplate;
        this.rLock = rLock;
    }

    @Override
    public void init() {
        // 绑定监听器
        bindListener();

        // 重新投递未被ack的消息
        unAckMsgRetryDeliver();
    }

    /**
     * 重新投递未被ack的消息
     */
    private void unAckMsgRetryDeliver() {
        ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(
                1,
                new CustomizableThreadFactory("notify-subscribeMsg#unAckRetry-pool-"));
        // 遍历获取未确认消息
        scheduler.scheduleWithFixedDelay(() -> {
            for (RedisSubMsgConsumer consumer : subMsgConsumers) {
                String lockKey = String.format(RedisConstant
                        .NOTIFY_SUBSCRIBE_LOCK_PREFIX, consumer.getKey() + consumer.getGroup());
                boolean lock = rLock.getLock(lockKey, redisConfig.getSubGroupTimeout());
                if (!lock) {
                    continue;
                }
                try {
                    loop(consumer);
                } catch (Exception ex) {
                    log.error(ex.getMessage(), ex);
                } finally {
                    try {
                        rLock.releaseLock(lockKey);
                    } catch (Exception var2) {
                        log.error(var2.getMessage(), var2);
                    }
                }
            }
        }, 5, 3, TimeUnit.SECONDS);
    }

    /**
     * 轮询获取消费者中待ack的消息
     *
     * @param consumer 消费者
     */
    private void loop(RedisSubMsgConsumer consumer) {
        StreamOperations<String, String, String> streamOperations = redisTemplate.opsForStream();
        // 获取my_group中的pending消息信息
        PendingMessagesSummary pendingSummary = streamOperations.pending(consumer.getKey(), consumer.getGroup());
        if (null == pendingSummary) {
            return;
        }
        // 所有pending消息的数量
        long totalMsgNum = pendingSummary.getTotalPendingMessages();
        if (totalMsgNum <= 0) {
            return;
        }

        log.debug("消费组：{}，一共有{}条pending消息...", consumer.getGroup(), totalMsgNum);
        // 每个消费者的pending消息数量
        Map<String, Long> pendingConsumers = pendingSummary.getPendingMessagesPerConsumer();

        pendingConsumers.forEach((consumerName, consumerMsgNum) -> {
            log.debug("消费者：{}，一共有{}条pending消息", consumerName, consumerMsgNum);
            if (consumerMsgNum <= 0) {
                return;
            }
            // 读取消费者pending队列的前10条记录，从ID=0的记录开始，一直到ID最大值
            PendingMessages pendingMessages = streamOperations.pending(
                    consumer.getKey(), Consumer.from(consumer.getGroup(), consumerName), Range.closed("0", "+"), redisConfig.getSubBatchSize());

            // 遍历所有pending消息的详情
            pendingMessages.forEach(message -> {
                // 消息的ID
                String recordId = message.getId().getValue();
                // 未达到订阅消息投递超时时间 不做处理
                Duration elapsedTimeSinceLastDelivery = message.getElapsedTimeSinceLastDelivery();
                if (elapsedTimeSinceLastDelivery.getSeconds() < redisConfig.getSubDeliverTimeout()) {
                    return;
                }
                // 消息被获取的次数
                long deliveryCount = message.getTotalDeliveryCount();

                log.info("pending消息，id={}, elapsedTimeSinceLastDelivery={}, deliveryCount={}", recordId, elapsedTimeSinceLastDelivery, deliveryCount);

                // 通过streamOperations，直接读取这条pending消息，
                List<ObjectRecord<String, String>> result =
                        streamOperations.range(String.class, consumer.getKey(), Range.closed(recordId, recordId));
                if (CollectionUtils.isEmpty(result)) {
                    return;
                }

                consumer.accept(result.get(0).getValue());
                // 如果手动消费成功后，往消费组提交消息的ACK
                streamOperations.acknowledge(consumer.getKey(), consumer.getGroup(), message.getId());
            });
        });
    }

    @SneakyThrows
    private void bindListener() {
        Executor executor = new ThreadPoolExecutor(
                redisConfig.getSubExecutorPoolSize(),
                redisConfig.getSubExecutorPoolSize(),
                1,
                TimeUnit.MINUTES,
                new LinkedBlockingDeque<>(),
                new CustomizableThreadFactory("notify-subscribeMsg-pool-"));
        // 创建配置对象
        var options
                = StreamMessageListenerContainer
                .StreamMessageListenerContainerOptions
                .builder()
                .executor(executor)
                // 一次性最多拉取多少条消息
                .batchSize(redisConfig.getSubBatchSize())
                // 消息消费异常的handler
                .errorHandler(t -> {
                    log.error("[MQ handler exception] ", t);
                })
                // 超时时间，设置为0，表示不超时（超时后会抛出异常）
                .pollTimeout(Duration.ZERO)
                // 序列化器
                .serializer(new StringRedisSerializer())
                .targetType(String.class)
                .build();

        // 根据配置对象创建监听容器对象
        listenerContainer = StreamMessageListenerContainer.create(redisConnectionFactory, options);
        for (RedisSubMsgConsumer consumer : subMsgConsumers) {
            // 初始化组
            createStreamGroup(redisTemplate, consumer);

            // 使用监听容器对象开始监听消费（使用的是手动确认方式）
            listenerContainer.receive(Consumer.from(consumer.getGroup(), InetAddress.getLocalHost().getHostName()),
                    StreamOffset.create(consumer.getKey(), ReadOffset.lastConsumed()),
                    message -> {
                        consumer.accept(message.getValue());
                        redisTemplate.opsForStream().acknowledge(consumer.getKey(), consumer.getGroup(), message.getId());
                    });
        }
        // 启动监听
        listenerContainer.start();
    }

    /**
     * destroy listener Container
     */
    @Override
    public void destroy() {
        this.listenerContainer.stop();
    }

    /**
     * 创建消费者组
     *
     * @param redisTemplate redisTemplate
     * @param consumer      消费者
     */
    private void createStreamGroup(RedisTemplate<String, String> redisTemplate, RedisSubMsgConsumer consumer) {
        boolean hasGroup = false;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(consumer.getKey()))) {
            StreamInfo.XInfoGroups groups = redisTemplate.opsForStream().groups(consumer.getKey());
            long count = groups.stream().filter(x -> consumer.getGroup().equals(x.groupName())).count();
            if (count > 0) {
                hasGroup = true;
            }
        }
        if (!hasGroup) {
            redisTemplate.opsForStream().createGroup(consumer.getKey(), consumer.getGroup());
        }
    }
}
