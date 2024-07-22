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

import com.github.likavn.eventbus.core.TaskRegistry;
import com.github.likavn.eventbus.core.base.Lifecycle;
import com.github.likavn.eventbus.core.metadata.support.Listener;
import com.github.likavn.eventbus.core.support.task.CronTask;
import com.github.likavn.eventbus.prop.BusProperties;
import com.github.likavn.eventbus.provider.redis.support.RedisListener;
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
public class RedisStreamExpiredTask implements Runnable, Lifecycle {
    /**
     * 数据清理定时任务，默认：每个小时21分进行清理
     */
    private static final String CRON = "0 21 1/1 * * ?";
    private final BusProperties busProperties;
    private final RLock rLock;
    private final StringRedisTemplate redisTemplate;
    private final List<RedisListener> redisSubscribers;
    private final DefaultRedisScript<Long> script;
    private boolean isMinVersion62 = false;
    private CronTask task;
    private final TaskRegistry taskRegistry;

    public RedisStreamExpiredTask(StringRedisTemplate redisTemplate,
                                  TaskRegistry taskRegistry,
                                  BusProperties busProperties,
                                  List<Listener> subscribers,
                                  RLock rLock) {
        this.busProperties = busProperties;
        this.taskRegistry = taskRegistry;
        this.rLock = rLock;
        this.redisTemplate = redisTemplate;

        // 及时消息订阅
        this.redisSubscribers = RedisListener.fullRedisSubscriber(subscribers, busProperties.getServiceId());
        String redisVersion = busProperties.getRedis().getRedisVersion();
        // 判断最低版本是否大于等于 6.2
        if (redisVersion.contains("-")) {
            redisVersion = redisVersion.substring(0, redisVersion.indexOf("-"));
        }
        String[] versions = redisVersion.split("\\.");
        String bigVersion = versions[0];
        if (bigVersion.compareTo("6") >= 0
                && (bigVersion.compareTo("7") >= 0 || (versions.length >= 2 && versions[1].compareTo("2") >= 0))) {
            isMinVersion62 = true;
        }
        // 过期消息处理脚本
        String cmd = isMinVersion62 ? "'MINID'" : "'MAXLEN', '~'";
        this.script = new DefaultRedisScript<>("return redis.call('XTRIM', KEYS[1]," + cmd + ", ARGV[1]);", Long.class);
    }

    @Override
    public void register() {
        task = CronTask.create(this.getClass().getName(), CRON, this);
        taskRegistry.createTask(task);
    }

    @Override
    public void run() {
        // stream 过期时间，单位：小时
        Long expiredHours = busProperties.getRedis().getStreamExpiredHours();
        // 过期时间毫秒数
        long expiredMillis = System.currentTimeMillis() - (1000L * 60 * 60 * expiredHours);
        redisSubscribers.stream().map(RedisListener::getStreamKey).distinct().forEach(streamKey
                -> cleanExpired(streamKey, expiredMillis + "-0"));
    }

    /**
     * 截取过期的消息
     */
    private void cleanExpired(String streamKey, String minId) {
        String lockKey = streamKey + ".deleteExpiredLock";
        boolean lock = rLock.getLock(lockKey);
        try {
            if (!lock) {
                return;
            }
            String param;
            if (isMinVersion62) {
                param = minId;
            } else {
                Long streamExpiredLength = busProperties.getRedis().getStreamExpiredLength();
                param = null == streamExpiredLength ? null : streamExpiredLength.toString();
            }
            if (null == param) {
                return;
            }
            Long deleteCount = redisTemplate.execute(script, Collections.singletonList(streamKey), param);
            log.debug("clean expired：streamKey={}, deleteCount={}", streamKey, deleteCount);
        } catch (Exception e) {
            log.error("clean expired：streamKey={}", streamKey, e);
        } finally {
            if (lock) {
                rLock.releaseLock(lockKey);
            }
        }
    }

    @Override
    public void destroy() {
        taskRegistry.removeTask(task);
    }
}
