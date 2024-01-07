package com.github.likavn.eventbus.provider.redis;

import com.github.likavn.eventbus.core.base.NetLifecycle;
import com.github.likavn.eventbus.core.constant.BusConstant;
import com.github.likavn.eventbus.prop.BusProperties;
import com.github.likavn.eventbus.provider.redis.constant.RedisConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.util.Arrays;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 延时消息处理类
 *
 * @author likavn
 * @since 2023/01/01
 */
@Slf4j
public class RedisMsgDelayListener implements NetLifecycle {
    /**
     * 轮询时间间隔，单位：毫秒
     */
    private static final long POLL_MILLIS = 500L;
    /**
     * 推送消息数量
     */
    private static final long PUSH_STREAM_COUNT = 1000L;

    private final RedisTemplate<String, String> redisTemplate;
    private final BusProperties busProperties;
    private final BusProperties.RedisProperties redisProperties;
    private final RLock rLock;
    private final String delayKey;
    private final String lockKey;
    private ScheduledThreadPoolExecutor scheduler;

    public RedisMsgDelayListener(RedisTemplate<String, String> redisTemplate,
                                 BusProperties busProperties,
                                 RLock rLock) {
        this.redisTemplate = redisTemplate;
        this.busProperties = busProperties;
        this.redisProperties = busProperties.getRedis();
        this.rLock = rLock;
        this.delayKey = String.format(RedisConstant.NOTIFY_DELAY_PREFIX, busProperties.getServiceId());
        this.lockKey = String.format(RedisConstant.NOTIFY_DELAY_LOCK_PREFIX, busProperties.getServiceId());
        register();
    }

    @Override
    public synchronized void register() {
        if (null == scheduler) {
            scheduler = new ScheduledThreadPoolExecutor(
                    1, new CustomizableThreadFactory(BusConstant.DELAY_MSG_THREAD_NAME));
        }
        scheduler.scheduleWithFixedDelay(this::loop, 0, POLL_MILLIS, TimeUnit.MILLISECONDS);
    }

    @Override
    public void destroy() throws Exception {
        if (null != scheduler) {
            scheduler.shutdown();
            scheduler = null;
        }
    }


    private DefaultRedisScript<Void> script = null;

    /**
     * 循环获取延时队列到期消息
     */
    private void loop() {
        // 获取锁
        boolean lock = rLock.getLock(lockKey);
        if (!lock) {
            return;
        }

        if (null == script) {
            //   script = new DefaultRedisScript<>(RedisLuaManage.getScript(RedisConstant.NOTIFY_DELAY_LOCK_PREFIX), Void.class);
        }

        redisTemplate.execute(script,
                Arrays.asList(
                        // 延时队列
                        delayKey,
                        // 订阅队列
                        String.format(RedisConstant.NOTIFY_SUBSCRIBE_DELAY_PREFIX, busProperties.getServiceId())),
                // 到当前时间之前的消息
                System.currentTimeMillis(),
                // 推送数量
                PUSH_STREAM_COUNT);
    }
}
