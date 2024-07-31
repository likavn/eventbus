/**
 * Copyright 2023-2033, likavn (likavn@163.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.likavn.eventbus.provider.redis;

import com.github.likavn.eventbus.core.ListenerRegistry;
import com.github.likavn.eventbus.core.TaskRegistry;
import com.github.likavn.eventbus.core.api.RequestIdGenerator;
import com.github.likavn.eventbus.core.base.AbstractSenderAdapter;
import com.github.likavn.eventbus.core.metadata.BusConfig;
import com.github.likavn.eventbus.core.metadata.InterceptorConfig;
import com.github.likavn.eventbus.core.metadata.data.Request;
import com.github.likavn.eventbus.core.support.task.Task;
import com.github.likavn.eventbus.core.utils.Func;
import com.github.likavn.eventbus.provider.redis.constant.RedisConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.Record;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;

/**
 * redis消息生产者
 *
 * @author likavn
 * @since 2023/01/01
 */
@Slf4j
public class RedisMsgSender extends AbstractSenderAdapter {
    private final StringRedisTemplate stringRedisTemplate;
    private final DefaultRedisScript<Long> zsetAddRedisScript;
    private final TaskRegistry taskRegistry;

    public RedisMsgSender(StringRedisTemplate stringRedisTemplate,
                          BusConfig config,
                          InterceptorConfig interceptorConfig,
                          DefaultRedisScript<Long> zsetAddRedisScript,
                          TaskRegistry taskRegistry, RequestIdGenerator requestIdGenerator, ListenerRegistry registry) {
        super(config, interceptorConfig, requestIdGenerator, registry);
        this.stringRedisTemplate = stringRedisTemplate;
        this.zsetAddRedisScript = zsetAddRedisScript;
        this.taskRegistry = taskRegistry;
    }

    @Override
    public void toSend(Request<?> request) {
        toSend(String.format(RedisConstant.BUS_SUBSCRIBE_PREFIX, request.topic()), request);
    }

    public void toSend(String streamKey, Request<?> request) {
        stringRedisTemplate.opsForStream().add(Record.of(Func.toJson(request)).withStreamKey(streamKey));
    }

    @Override
    public void toSendDelayMessage(Request<?> request) {
        // 计算延迟时间
        Long timeMillis = System.currentTimeMillis() + (1000L * request.getDelayTime());
        timeMillis = stringRedisTemplate.execute(zsetAddRedisScript,
                Collections.singletonList(String.format(RedisConstant.BUS_DELAY_PREFIX, request.getServiceId())),
                // 到当前时间之前的消息 + 推送数量
                String.valueOf(timeMillis), Func.toJson(request));
        // 重置延迟任务
        setNextTriggerTimeMillis(timeMillis);
    }

    /**
     * 重置轮询时间
     */
    public void setNextTriggerTimeMillis(Long timeMillis) {
        if (null == timeMillis) {
            return;
        }
        Task task = taskRegistry.getTask(RedisMsgDelayListener.class.getName());
        if (null != task) {
            task.refreshNextExecutionTime(timeMillis);
        }
    }
}
