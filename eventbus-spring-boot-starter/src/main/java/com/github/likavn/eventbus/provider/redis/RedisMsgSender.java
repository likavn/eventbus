package com.github.likavn.eventbus.provider.redis;

import com.github.likavn.eventbus.core.base.AbstractSenderAdapter;
import com.github.likavn.eventbus.core.metadata.BusConfig;
import com.github.likavn.eventbus.core.metadata.InterceptorConfig;
import com.github.likavn.eventbus.core.metadata.data.Request;
import com.github.likavn.eventbus.core.utils.Func;
import com.github.likavn.eventbus.provider.redis.constant.RedisConstant;
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
public class RedisMsgSender extends AbstractSenderAdapter {
    private final BusConfig config;
    private final RedisTemplate<String, String> redisTemplate;

    public RedisMsgSender(InterceptorConfig interceptorConfig, BusConfig config, RedisTemplate<String, String> redisTemplate) {
        super(interceptorConfig, config);
        this.config = config;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void toSend(Request<?> request) {
        redisTemplate.opsForStream().add(Record.of(Func.toJson(request))
                .withStreamKey(String.format(RedisConstant.NOTIFY_SUBSCRIBE_PREFIX, request.getTopic())));
    }

    @Override
    public void toSendDelayMessage(Request<?> request) {
        redisTemplate.opsForZSet().add(String.format(RedisConstant.NOTIFY_DELAY_PREFIX, config.getServiceId()),
                Func.toJson(request), (System.currentTimeMillis() + (1000L * request.getDelayTime())));
    }
}
