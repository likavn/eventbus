package com.github.likavn.notify.provider.redis;

import com.github.likavn.notify.base.BaseDelayMsgHandler;
import com.github.likavn.notify.prop.NotifyProperties;
import com.github.likavn.notify.provider.redis.constant.RedisConstant;
import com.github.likavn.notify.utils.SpringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * 延时消息处理类
 *
 * @author likavn
 * @since 2023/01/01
 */
@SuppressWarnings("all")
public class RedisDelayMsgListener extends BaseDelayMsgHandler {
    private static final Logger logger = LoggerFactory.getLogger(RedisDelayMsgListener.class);

    /**
     * 轮询时间间隔，单位：毫秒
     */
    private static final long POLL_MILLIS = 500L;

    private final ZSetOperations zSetOps;

    private final RLock rLock;

    private final NotifyProperties notifyProperties;

    public RedisDelayMsgListener(RedisTemplate<String, String> redisTemplate,
                                 RLock rLock,
                                 NotifyProperties notifyProperties) {
        this.zSetOps = redisTemplate.opsForZSet();
        this.rLock = rLock;
        this.notifyProperties = notifyProperties;
        new Thread(() -> loop()).start();
    }

    /**
     * 初始化
     */
    private void loop() {
        NotifyProperties.Redis redisConfig = notifyProperties.getRedis();
        String key = String.format(RedisConstant.NOTIFY_DELAY_PREFIX, SpringUtil.getServiceId());
        while (!Thread.interrupted()) {
            // 当前时间
            long currentTimeMillis = System.currentTimeMillis();
            Set<String> values = zSetOps.rangeByScore(key, 0, currentTimeMillis, 0, 100);
            if (null == values || values.isEmpty()) {
                try {
                    Thread.sleep(POLL_MILLIS);
                } catch (InterruptedException e) {
                    break;
                }
                continue;
            }
            values.parallelStream().forEach(value -> {
                String lockKey = String.format(RedisConstant
                        .NOTIFY_DELAY_LOCK_PREFIX, DigestUtils.md5DigestAsHex(value.getBytes(StandardCharsets.UTF_8)));
                boolean lock = false;
                try {
                    // 消息延时
                    zSetOps.incrementScore(key, value, POLL_MILLIS * 2);
                    // 获取锁
                    lock = rLock.getLock(lockKey, redisConfig.getDelayTimeout());
                    if (!lock) {
                        return;
                    }

                    deliver(value);
                    // 删除value
                    zSetOps.remove(key, value);
                } catch (Exception ex) {
                    logger.error(ex.getMessage(), ex);
                } finally {
                    if (lock) {
                        try {
                            rLock.releaseLock(lockKey);
                        } catch (Exception var2) {
                            logger.error(var2.getMessage(), var2);
                        }
                    }
                }
            });
        }
    }

}
