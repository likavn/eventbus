package com.github.likavn.eventbus.provider.redis;

import com.github.likavn.eventbus.core.base.NodeTestConnect;
import com.github.likavn.eventbus.core.metadata.BusConfig;
import io.lettuce.core.RedisCommandTimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * redis连接状态测试
 *
 * @author likavn
 * @date 2023/5/30
 **/
@Slf4j
public class RedisNodeTestConnect implements NodeTestConnect {

    private final StringRedisTemplate stringRedisTemplate;
    private final String testKey;

    public RedisNodeTestConnect(StringRedisTemplate stringRedisTemplate, BusConfig busConfig) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.testKey = String.format("eventbus.{%s}", busConfig.getServiceId());
    }

    @Override
    public boolean testConnect() {
        try {
            stringRedisTemplate.hasKey(testKey);
        } catch (RedisCommandTimeoutException ex) {
            log.error("redis timeout", ex);
            return false;
        }
        return true;
    }
}
