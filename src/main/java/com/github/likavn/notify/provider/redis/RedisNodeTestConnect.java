package com.github.likavn.notify.provider.redis;

import com.github.likavn.notify.base.NodeTestConnect;
import io.lettuce.core.RedisCommandTimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @author likavn
 * @date 2023/5/30
 **/
@Slf4j
public class RedisNodeTestConnect implements NodeTestConnect {

    private final RedisTemplate<String, String> redisTemplate;

    public RedisNodeTestConnect(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean testConnect() {
        try {
            redisTemplate.hasKey("notify.test");
        } catch (RedisCommandTimeoutException ex) {
            log.error("redis timeout", ex);
            return false;
        }
        return true;
    }
}
