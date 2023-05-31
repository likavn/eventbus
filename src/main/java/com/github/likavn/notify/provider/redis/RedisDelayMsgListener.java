package com.github.likavn.notify.provider.redis;

import com.github.likavn.notify.base.AbstractMsgDelayHandler;
import com.github.likavn.notify.prop.NotifyProperties;
import com.github.likavn.notify.provider.redis.constant.RedisConstant;
import com.github.likavn.notify.utils.Func;
import com.github.likavn.notify.utils.SpringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 延时消息处理类
 *
 * @author likavn
 * @since 2023/01/01
 */
@Slf4j
public class RedisDelayMsgListener extends AbstractMsgDelayHandler {
    /**
     * 轮询时间间隔，单位：毫秒
     */
    private static final long POLL_MILLIS = 500L;

    private final ZSetOperations<String, String> zSetOps;

    private final RLock rLock;

    private final NotifyProperties.Redis redisConfig;

    private final NotifyProperties notifyProperties;

    private final String delayKey;

    private ScheduledThreadPoolExecutor scheduler;

    public RedisDelayMsgListener(RedisTemplate<String, String> redisTemplate,
                                 RLock rLock,
                                 NotifyProperties notifyProperties) {
        this.zSetOps = redisTemplate.opsForZSet();
        this.rLock = rLock;
        this.notifyProperties = notifyProperties;
        this.redisConfig = notifyProperties.getRedis();
        this.delayKey = String.format(RedisConstant.NOTIFY_DELAY_PREFIX, SpringUtil.getServiceId());
    }

    @Override
    public void register() {
        int delayConsumerNum = notifyProperties.getDelayConsumerNum();
        if (null == scheduler) {
            scheduler = new ScheduledThreadPoolExecutor(
                    delayConsumerNum, new CustomizableThreadFactory("notify-delayMsg-pool-"));
        }
        while (delayConsumerNum-- > 0) {
            scheduler.scheduleWithFixedDelay(this::loop, 0, POLL_MILLIS, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * 循环获取延时队列到期消息
     */
    private void loop() {
        // 当前时间
        long currentTimeMillis = System.currentTimeMillis();
        Set<String> values = zSetOps.rangeByScore(delayKey, 0, currentTimeMillis, 0, 100);
        if (null == values || values.isEmpty()) {
            return;
        }
        values.parallelStream().forEach(value -> {
            String lockKey = String.format(RedisConstant
                    .NOTIFY_DELAY_LOCK_PREFIX, DigestUtils.md5DigestAsHex(value.getBytes(StandardCharsets.UTF_8)));
            boolean lock = false;
            try {
                // 消息延时
                zSetOps.incrementScore(delayKey, value, POLL_MILLIS * 2.0);
                // 获取锁
                lock = rLock.getLock(lockKey, redisConfig.getDelayDeliverTimeout());
                if (!lock) {
                    return;
                }

                deliver(value);
                // 删除value
                zSetOps.remove(delayKey, value);
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            } finally {
                if (lock) {
                    try {
                        rLock.releaseLock(lockKey);
                    } catch (Exception var2) {
                        log.error(var2.getMessage(), var2);
                    }
                }
            }
        });
    }

    @Override
    public void destroy() {
        Func.resetPool(scheduler);
    }

}
