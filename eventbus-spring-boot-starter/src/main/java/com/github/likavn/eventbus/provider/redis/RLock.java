package com.github.likavn.eventbus.provider.redis;

import org.springframework.data.redis.core.StringRedisTemplate;
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
    private final StringRedisTemplate stringRedisTemplate;
    private final DefaultRedisScript<Boolean> lockRedisScript;

    public RLock(StringRedisTemplate stringRedisTemplate, DefaultRedisScript<Boolean> lockRedisScript) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.lockRedisScript = lockRedisScript;
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
        Boolean flag = stringRedisTemplate.execute(lockRedisScript, Collections.singletonList(key), "" + timeout);
        return Boolean.TRUE.equals(flag);
    }

    /**
     * 释放锁
     *
     * @param key k
     */
    public void releaseLock(String key) {
        stringRedisTemplate.delete(key);
    }
}
