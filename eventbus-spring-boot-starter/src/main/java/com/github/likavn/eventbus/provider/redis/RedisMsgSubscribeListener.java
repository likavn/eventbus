package com.github.likavn.eventbus.provider.redis;

import com.github.likavn.eventbus.core.DeliveryBus;
import com.github.likavn.eventbus.core.metadata.support.Subscriber;
import com.github.likavn.eventbus.core.utils.Func;
import com.github.likavn.eventbus.prop.BusProperties;
import com.github.likavn.eventbus.provider.redis.constant.RedisConstant;
import com.github.likavn.eventbus.provider.redis.support.AbstractDefaultStreamContainer;
import com.github.likavn.eventbus.provider.redis.support.RedisSubscriber;
import com.github.likavn.eventbus.provider.redis.support.XStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;

import java.util.List;
import java.util.stream.Collectors;

/**
 * redis消息监听器
 *
 * @author likavn
 * @since 2023/01/01
 **/
@Slf4j
public class RedisMsgSubscribeListener extends AbstractDefaultStreamContainer {
    private final BusProperties busProperties;
    private final List<RedisSubscriber> subscribers;
    private final DeliveryBus deliveryBus;
    private final StringRedisTemplate stringRedisTemplate;

    public RedisMsgSubscribeListener(StringRedisTemplate stringRedisTemplate, BusProperties busProperties,
                                     List<Subscriber> subscribers, DeliveryBus deliveryBus) {
        super(stringRedisTemplate, busProperties);
        this.busProperties = busProperties;
        this.subscribers = subscribers.stream().map(t -> new RedisSubscriber(t, RedisConstant.NOTIFY_SUBSCRIBE_PREFIX)).collect(Collectors.toList());
        this.deliveryBus = deliveryBus;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    protected void addReceives(StreamMessageListenerContainer<String, ObjectRecord<String, String>> listenerContainer) {
        String hostName = Func.getHostName();
        subscribers.forEach(subscriber -> {
            // 初始化组
            XStream.addConsumerGroup(stringRedisTemplate, subscriber);
            int num = 1;
            while (num++ <= busProperties.getConsumerCount()) {
                // 使用监听容器对象开始监听消费（使用的是手动确认方式）
                listenerContainer.receive(Consumer.from(subscriber.getGroup(), hostName + "-" + num),
                        StreamOffset.create(subscriber.getStreamKey(), ReadOffset.lastConsumed()),
                        msg -> {
                            deliveryBus.deliver(subscriber, msg.getValue());
                            stringRedisTemplate.opsForStream().acknowledge(subscriber.getStreamKey(), subscriber.getGroup(), msg.getId());
                        });
            }
        });
    }
}
