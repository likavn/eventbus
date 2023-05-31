package com.github.likavn.notify.provider.redis;

import com.github.likavn.notify.base.AbstractMsgSender;
import com.github.likavn.notify.domain.Request;
import com.github.likavn.notify.provider.redis.constant.RedisConstant;
import com.github.likavn.notify.utils.Func;
import com.github.likavn.notify.utils.SpringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.Record;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * redis消息生产者
 *
 * @author likavn
 * @since 2023/01/01
 */
@Slf4j
public class RedisMsgSender extends AbstractMsgSender {

    private final RedisTemplate<String, String> redisTemplate;

    public RedisMsgSender(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void toSend(Request<?> request) {
        redisTemplate.opsForStream().add(Record.of(Func.toJson(request))
                .withStreamKey(String.format(RedisConstant.NOTIFY_SUBSCRIBE_PREFIX, request.getTopic())));
    }

    @Override
    public void toSendDelayMessage(Request<?> request) {
        redisTemplate.opsForZSet().add(String.format(RedisConstant.NOTIFY_DELAY_PREFIX, SpringUtil.getServiceId()),
                Func.toJson(request), System.currentTimeMillis() + (1000L * request.getDelayTime()));
    }
}
