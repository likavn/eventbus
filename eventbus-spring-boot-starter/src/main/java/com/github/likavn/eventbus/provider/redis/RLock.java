package com.github.likavn.eventbus.provider.redis;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;

/**
 * redis分布式锁
 *
 * @author likavn
 * @date 2023/2/22
 **/
public class RLock {
    /**
     * 分布式锁过期时间,单位：秒
     */
    private static final Long LOCK_REDIS_TIMEOUT = 30L;
    private final RedisTemplate<String, String> redisTemplate;
    private final DefaultRedisScript<Integer> lockDefaultRedisScript;

    public RLock(RedisTemplate<String, String> redisTemplate, DefaultRedisScript<Integer> lockDefaultRedisScript) {
        this.redisTemplate = redisTemplate;
        this.lockDefaultRedisScript = lockDefaultRedisScript;
    }

    /**
     * 获取锁,默认超时时间30s
     *
     * @param key key
     * @return t
     */
    public boolean getLock(String key) {
        return getLock(key, LOCK_REDIS_TIMEOUT);
    }

    /**
     * 获取锁
     *
     * @param key     key
     * @param timeout 超时时间，单位：秒
     * @return t
     */
    public boolean getLock(String key, long timeout) {
        Integer value = redisTemplate.execute(new DefaultRedisScript<>("return redis.call('EXISTS', lockKey)"), Collections.singletonList(key), null);
        if (null == value || value != 1) {
            return false;
        }
        return true;
    }

    /**
     * 释放锁
     *
     * @param key k
     */
    public void releaseLock(String key) {
        redisTemplate.delete(key);
    }
}
