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
import com.github.likavn.eventbus.core.base.Lifecycle;
import com.github.likavn.eventbus.core.support.task.PeriodTask;
import com.github.likavn.eventbus.core.utils.Func;
import com.github.likavn.eventbus.provider.redis.support.RedisListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.*;
import java.util.stream.Collectors;

/**
 * redis 推送zset消息至stream队列任务
 *
 * @author likavn
 * @since 2023/01/01
 **/
@Slf4j
public class RedisZSetPushMsgStreamTask implements Lifecycle {
    /**
     * 最大轮询时间间隔，单位：毫秒
     */
    private static final long POLL_MILLIS = 1000L * 100;
    /**
     * 最大消息推送数量，默认10万条
     */
    private static final long MAX_PUSH_COUNT = 10000L * 10;

    private final StringRedisTemplate stringRedisTemplate;
    private final RLock rLock;
    private final DefaultRedisScript<Long> pushMsgStreamRedisScript;
    private final TaskRegistry taskRegistry;
    private final List<RedisListener> listeners;
    private final Set<String> pollLockKeys = Collections.synchronizedSet(new HashSet<>());
    private List<PeriodTask> tasks;

    public RedisZSetPushMsgStreamTask(StringRedisTemplate stringRedisTemplate,
                                      TaskRegistry taskRegistry, DefaultRedisScript<Long> pushMsgStreamRedisScript, RLock rLock, ListenerRegistry registry) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.taskRegistry = taskRegistry;
        this.pushMsgStreamRedisScript = pushMsgStreamRedisScript;
        this.rLock = rLock;
        this.listeners = Func.distinct(RedisListener.getAllListeners(registry), RedisListener::getZSetKey)
                .stream()
                // zSetKey必须配置
                .filter(t -> t.getZSetKey() != null)
                .collect(Collectors.toList());
    }

    @Override
    public synchronized void register() {
        this.tasks = listeners.stream().map(t -> {
            PeriodTask task = PeriodTask.create(t.getZSetKey(), POLL_MILLIS, null);
            task.setRunnable(() -> pollPushTask(task, t));
            return task;
        }).collect(Collectors.toList());
        tasks.forEach(taskRegistry::createTask);
    }

    /**
     * 循环获取延时队列到期消息
     */
    private void pollPushTask(PeriodTask task, RedisListener listener) {
        boolean isLock = false;
        try {
            isLock = rLock.getLock(listener.getLockKey());
            if (!isLock) {
                return;
            }
            pollLockKeys.add(listener.getLockKey());
            Long nextCurrentTimeMillis = stringRedisTemplate.execute(pushMsgStreamRedisScript,
                    Arrays.asList(listener.getZSetKey(), listener.getStreamKey()),
                    // 到当前时间之前的消息 + 推送数量
                    String.valueOf(System.currentTimeMillis()), String.valueOf(MAX_PUSH_COUNT));
            // 重置轮询时间
            if (null != nextCurrentTimeMillis) {
                task.refreshNextExecutionTime(nextCurrentTimeMillis);
            }
        } finally {
            if (isLock) {
                rLock.releaseLock(listener.getLockKey());
                pollLockKeys.remove(listener.getLockKey());
            }
        }
    }

    @Override
    public void destroy() {
        // 释放锁
        Iterator<String> iterator = pollLockKeys.iterator();
        while (iterator.hasNext()) {
            String pollLockKey = iterator.next();
            try {
                rLock.releaseLock(pollLockKey);
            } catch (Exception e) {
                log.error("release pollLockKey error", e);
            }
            iterator.remove();
        }
        tasks.forEach(taskRegistry::removeTask);
    }
}
