package com.github.likavn.notify.provider.redis;

import com.github.likavn.notify.base.DefaultMsgSender;
import com.github.likavn.notify.constant.MsgConstant;
import com.github.likavn.notify.domain.MsgRequest;
import com.github.likavn.notify.utils.SpringUtil;
import com.github.likavn.notify.utils.WrapUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Instant;

/**
 * 通知生产者
 *
 * @author likavn
 * @since 2023/01/01
 */
@Slf4j
public class RedisMsgSender extends DefaultMsgSender {

    private final RedisTemplate<String, String> redisTemplate;

    public RedisMsgSender(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void send(MsgRequest<?> request) {
        before(request);
        redisTemplate.convertAndSend(request.getServiceId() + "|" + request.getCode(), WrapUtils.toJson(request));
    }

    @Override
    @SuppressWarnings("all")
    public void sendDelayMessage(MsgRequest<?> request, long delayTime) {
        before(request);
        redisTemplate.opsForZSet().add(
                String.format(MsgConstant.REDIS_Z_SET_KEY, SpringUtil.getServiceId()),
                WrapUtils.toJson(request),
                Instant.now().getEpochSecond() + delayTime);
    }
}
