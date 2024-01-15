package com.github.likavn.eventbus.provider.redis.support;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.StreamInfo;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * redis stream 工具类
 *
 * @author likavn
 * @date 2024/1/9
 **/
@Slf4j
public class XStream {
    /**
     * 创建消费者组
     *
     * @param stringRedisTemplate redisTemplate
     * @param consumer            消费者
     */
    public static void addConsumerGroup(StringRedisTemplate stringRedisTemplate, RedisSubscriber consumer) {
        addConsumerGroup(stringRedisTemplate, consumer.getStreamKey(), consumer.getGroup());
    }

    /**
     * 创建消费者组
     *
     * @param stringRedisTemplate redisTemplate
     * @param streamKey           streamKey
     * @param group               group
     */
    public static void addConsumerGroup(StringRedisTemplate stringRedisTemplate, String streamKey, String group) {
        boolean hasGroup = false;
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(streamKey))) {
            StreamInfo.XInfoGroups groups = stringRedisTemplate.opsForStream().groups(streamKey);
            long count = groups.stream().filter(x -> group.equals(x.groupName())).count();
            if (count > 0) {
                hasGroup = true;
            }
        }
        if (!hasGroup) {
            stringRedisTemplate.opsForStream().createGroup(streamKey, group);
        }
    }
}
