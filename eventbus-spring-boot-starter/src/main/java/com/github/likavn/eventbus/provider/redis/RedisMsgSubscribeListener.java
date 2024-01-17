package com.github.likavn.eventbus.provider.redis;

import com.github.likavn.eventbus.core.DeliveryBus;
import com.github.likavn.eventbus.core.constant.BusConstant;
import com.github.likavn.eventbus.core.metadata.support.Subscriber;
import com.github.likavn.eventbus.prop.BusProperties;
import com.github.likavn.eventbus.provider.redis.constant.RedisConstant;
import com.github.likavn.eventbus.provider.redis.support.AbstractStreamListenerContainer;
import com.github.likavn.eventbus.provider.redis.support.RedisSubscriber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.Record;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;
import java.util.stream.Collectors;

/**
 * redis消息监听器
 *
 * @author likavn
 * @since 2023/01/01
 **/
@Slf4j
public class RedisMsgSubscribeListener extends AbstractStreamListenerContainer {
    private final List<RedisSubscriber> subscribers;
    private final DeliveryBus deliveryBus;

    public RedisMsgSubscribeListener(StringRedisTemplate stringRedisTemplate,
                                     BusProperties busProperties,
                                     List<Subscriber> subscribers,
                                     DeliveryBus deliveryBus) {
        super(stringRedisTemplate, busProperties, BusConstant.SUBSCRIBE_MSG_THREAD_NAME);
        this.deliveryBus = deliveryBus;
        this.subscribers = subscribers.stream().map(t
                -> new RedisSubscriber(t, RedisConstant.BUS_SUBSCRIBE_PREFIX)).collect(Collectors.toList());
    }

    @Override
    protected List<RedisSubscriber> getSubscribers() {
        return this.subscribers;
    }

    @Override
    protected void deliver(RedisSubscriber subscriber, Record<String, String> msg) {
        deliveryBus.deliver(subscriber, msg.getValue());
        stringRedisTemplate.opsForStream().acknowledge(subscriber.getStreamKey(), subscriber.getGroup(), msg.getId());
    }
}
