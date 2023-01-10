package com.github.likavn.notify.provider.redis;

import com.github.likavn.notify.base.BaseDelayMsgListener;
import com.github.likavn.notify.constant.MsgConstant;
import com.github.likavn.notify.utils.SpringUtil;
import com.github.likavn.notify.utils.WrapUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.time.Instant;
import java.util.Set;

/**
 * 延时消息处理类
 *
 * @author likavn
 * @since 2023/01/01
 */
@Slf4j
public class RedisDelayMsgListener extends BaseDelayMsgListener {

    private final ZSetOperations zSetOps;

    @SuppressWarnings("all")
    public RedisDelayMsgListener(RedisTemplate<String, String> redisTemplate) {
        this.zSetOps = redisTemplate.opsForZSet();
        new Thread(() -> loop()).start();
    }

    /**
     * 初始化
     */
    @SuppressWarnings("all")
    private void loop() {
        String key = String.format(MsgConstant.REDIS_Z_SET_KEY, SpringUtil.getAppName());
        while (!Thread.interrupted()) {
            // 当前时间
            long nowSecond = Instant.now().getEpochSecond();
            Set<String> values = zSetOps.rangeByScore(key, 0, nowSecond, 0, 100);
            if (null == values || values.isEmpty()) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    break;
                }
                continue;
            }
            values.parallelStream().forEach(value -> {
                // 删除value
                if (zSetOps.remove(key, value) < 0) {
                    return;
                }

                try {
                    handler(WrapUtils.convertByBytes(value));
                } catch (Exception ex) {
                    log.error("DelayMessageListener", ex);
                }
            });
        }
    }

}
