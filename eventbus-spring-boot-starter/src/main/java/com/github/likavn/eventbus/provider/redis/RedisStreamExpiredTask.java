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

import com.github.likavn.eventbus.core.metadata.support.Subscriber;
import com.github.likavn.eventbus.prop.BusProperties;
import com.github.likavn.eventbus.provider.redis.support.RedisSubscriber;
import com.github.likavn.eventbus.schedule.CronTask;
import com.github.likavn.eventbus.schedule.ScheduledTaskRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;
import java.util.List;

/**
 * redis stream过期消息处理
 *
 * @author likavn
 * @date 2024/3/26
 **/
@Slf4j
public class RedisStreamExpiredTask extends CronTask {
    /**
     * 数据清理定时任务，默认：每个小时21分进行清理
     */
    private static final String CRON = "0 21 1/1 * * ?";
    private final BusProperties busProperties;
    private final RLock rLock;
    private final StringRedisTemplate redisTemplate;
    private final List<RedisSubscriber> redisSubscribers;
    private final DefaultRedisScript<Long> script;

    public RedisStreamExpiredTask(StringRedisTemplate redisTemplate,
                                  ScheduledTaskRegistry taskRegistry,
                                  BusProperties busProperties,
                                  List<Subscriber> subscribers,
                                  RLock rLock) {
        super(taskRegistry, RedisStreamExpiredTask.class.getName(), CRON);
        this.busProperties = busProperties;
        this.rLock = rLock;
        this.redisTemplate = redisTemplate;

        // 及时消息订阅
        this.redisSubscribers = RedisSubscriber.fullRedisSubscriber(subscribers, busProperties.getServiceId());

        // 过期消息处理脚本
        this.script = new DefaultRedisScript<>("return redis.call('XTRIM', KEYS[1],'MINID', ARGV[1]);", Long.class);
    }

    @Override
    public void run() {
        // stream 过期时间，单位：小时
        Long expiredHours = busProperties.getRedis().getStreamExpiredHours();
        // 过期时间毫秒数
        long expiredMillis = System.currentTimeMillis() - (1000L * 60 * 60 * expiredHours);
        redisSubscribers.stream().map(RedisSubscriber::getStreamKey).distinct().forEach(streamKey -> {
            deleteExpired(streamKey, expiredMillis + "-0");
        });
    }

    /**
     * 截取过期的消息
     */
    private void deleteExpired(String streamKey, String minId) {
        String lockKey = streamKey + ".deleteExpiredLock";
        boolean lock = rLock.getLock(lockKey);
        try {
            if (!lock) {
                return;
            }
            Long deleteCount = redisTemplate.execute(script, Collections.singletonList(streamKey), minId);
            log.debug("删除过期消息：streamKey={}, deleteCount={}", streamKey, deleteCount);
        } catch (Exception e) {
            log.error("删除过期消息失败：streamKey={}", streamKey, e);
        } finally {
            if (lock) {
                rLock.releaseLock(lockKey);
            }
        }
    }
}
