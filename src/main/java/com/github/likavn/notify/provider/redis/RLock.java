package com.github.likavn.notify.provider.redis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

import java.time.Duration;
import java.util.Collections;

/**
 * redis分布式锁
 *
 * @author likavn
 * @date 2023/2/22
 **/
@Slf4j
@SuppressWarnings("all")
public class RLock {
    private RedisTemplate redisTemplate;

    /**
     * 分布式锁过期时间 s  可以根据业务自己调节
     */
    private static final Long LOCK_REDIS_TIMEOUT = 15L;

    public RLock(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 获取锁
     *
     * @param key key
     * @return t
     */
    public Boolean getLock(String key) {
        return redisTemplate.opsForValue().setIfAbsent(key, "", Duration.ofSeconds(LOCK_REDIS_TIMEOUT));
    }

    /**
     * 获取锁
     *
     * @param key     key
     * @param timeout 超时时间，单位：秒
     * @return t
     */
    public Boolean getLock(String key, long timeout) {
        return redisTemplate.opsForValue().setIfAbsent(key, "", Duration.ofSeconds(timeout));
    }

    /**
     * 释放锁
     *
     * @param key k
     * @return
     */
    public Long releaseLock(String key) {
        String luaScript = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        RedisScript<Long> redisScript = new DefaultRedisScript<>(luaScript, Long.class);
        Long releaseStatus = (Long) this.redisTemplate.execute(redisScript, Collections.singletonList(key), "");
        return releaseStatus;
    }
}
