package com.github.likavn.eventbus.redis;

import com.github.likavn.eventbus.core.base.AbstractSenderAdapter;
import com.github.likavn.eventbus.core.exception.EventBusException;
import com.github.likavn.eventbus.core.metadata.BusConfig;
import com.github.likavn.eventbus.core.metadata.InterceptorConfig;
import com.github.likavn.eventbus.core.metadata.data.Request;
import com.github.likavn.eventbus.core.utils.Func;
import com.github.likavn.eventbus.redis.constant.RedisConstant;
import io.lettuce.core.ScoredValue;
import io.lettuce.core.api.StatefulRedisConnection;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPool;

/**
 * redis消息生产者
 *
 * @author likavn
 * @since 2023/01/01
 */
@Slf4j
public class RedisMsgSender extends AbstractSenderAdapter {

    private final GenericObjectPool<StatefulRedisConnection<String, String>> pool;

    public RedisMsgSender(GenericObjectPool<StatefulRedisConnection<String, String>> pool,
                          InterceptorConfig interceptorConfig,
                          BusConfig config) {
        super(interceptorConfig, config);
        this.pool = pool;
    }

    @Override
    public void toSend(Request<?> request) {
        StatefulRedisConnection<String, String> connection = null;
        try {
            connection = pool.borrowObject();
            connection.sync().xadd(String.format(RedisConstant.NOTIFY_SUBSCRIBE_PREFIX, request.getTopic()), Func.toJson(request));
        } catch (Exception e) {
            throw new EventBusException(e);
        } finally {
            if (null != connection) {
                pool.returnObject(connection);
            }
        }
    }

    @Override
    public void toSendDelayMessage(Request<?> request) {
        double score = System.currentTimeMillis() + (1000L * request.getDelayTime());
        StatefulRedisConnection<String, String> connection = null;
        try {
            connection = pool.borrowObject();
            connection.sync().zadd(String.format(RedisConstant.NOTIFY_SUBSCRIBE_PREFIX, request.getTopic()), ScoredValue.just(score, Func.toJson(request)));
        } catch (Exception e) {
            throw new EventBusException(e);
        } finally {
            if (null != connection) {
                pool.returnObject(connection);
            }
        }
    }
}
