package com.github.likavn.eventbus.provider.redis;

import com.github.likavn.eventbus.core.base.AbstractSenderAdapter;
import com.github.likavn.eventbus.core.metadata.BusConfig;
import com.github.likavn.eventbus.core.metadata.InterceptorConfig;
import com.github.likavn.eventbus.core.metadata.data.Request;
import com.github.likavn.eventbus.core.utils.Func;
import com.github.likavn.eventbus.provider.redis.constant.RedisConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.Record;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * redis消息生产者
 *
 * @author likavn
 * @since 2023/01/01
 */
@Slf4j
public class RedisMsgSender extends AbstractSenderAdapter {
    private final StringRedisTemplate stringRedisTemplate;

    public RedisMsgSender(StringRedisTemplate stringRedisTemplate, BusConfig config, InterceptorConfig interceptorConfig) {
        super(config, interceptorConfig);
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public void toSend(Request<?> request) {
        toSend(String.format(RedisConstant.BUS_SUBSCRIBE_PREFIX, request.getTopic()), request);
    }

    public void toSend(String streamKey, Request<?> request) {
        stringRedisTemplate.opsForStream().add(Record.of(Func.toJson(request)).withStreamKey(streamKey));
    }

    @Override
    public void toSendDelayMessage(Request<?> request) {
        stringRedisTemplate.opsForZSet().add(String.format(RedisConstant.BUS_DELAY_PREFIX, request.getServiceId()),
                Func.toJson(request), (System.currentTimeMillis() + (1000L * request.getDelayTime())));
    }
}
