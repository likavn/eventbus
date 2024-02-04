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
package com.github.likavn.eventbus.schedule;

import com.github.likavn.eventbus.core.utils.Assert;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.PeriodicTrigger;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * ScheduledTaskRegistry 任务注册中心
 *
 * @author likavn
 * @date 2024/1/11
 **/
@Slf4j
public class ScheduledTaskRegistry {
    /**
     * 任务调度器
     */
    private final ThreadPoolTaskScheduler scheduler;

    /**
     * 注册任务
     */
    private final Map<String, ScheduledTaskHolder> register = new ConcurrentHashMap<>();

    public ScheduledTaskRegistry(ThreadPoolTaskScheduler scheduler) {
        this.scheduler = scheduler;
    }

    /**
     * 创建任务
     */
    public synchronized void createTask(Task task) {
        Assert.isTrue(!register.containsKey(task.getName()), "任务已经存在");

        ScheduledFuture<?> future = this.scheduler.schedule(task, createTrigger(task));
        ScheduledTaskHolder holder = new ScheduledTaskHolder();
        holder.setScheduledFuture(future);
        holder.setTask(task);
        register.put(task.getName(), holder);
    }

    /**
     * 判断任务是否存在
     */
    public boolean containsTask(Task task) {
        return register.containsKey(task.getName());
    }

    /**
     * 获取任务
     */
    public Task getTask(String taskName) {
        ScheduledTaskHolder holder = register.get(taskName);
        return null == holder ? null : holder.getTask();
    }

    /**
     * 查询所有任务
     */
    public Collection<ScheduledTaskHolder> list() {
        return register.values();
    }

    /**
     * 暂停任务
     */
    public void pause(Task task) {
        ScheduledTaskHolder holder = register.get(task.getName());
        if (holder != null) {
            if (!holder.terminate()) {
                holder.getScheduledFuture().cancel(true);
            }
        } else {
            log.warn("尝试取消不存在的任务: {}", task.getName());
        }
    }

    /**
     * 重启任务
     */
    public void restart(Task task) {
        ScheduledTaskHolder holder = register.get(task.getName());
        Assert.notNull(holder, "任务不存在");
        if (!holder.terminate()) {
            // 暂停原任务
            holder.getScheduledFuture().cancel(true);
        }
        ScheduledFuture<?> future = this.scheduler.schedule(task, createTrigger(task));
        holder.setScheduledFuture(future);
    }

    /**
     * 创建触发器
     */
    private Trigger createTrigger(Task task) {
        Assert.notNull(task, "任务不能为空");
        // cron任务
        if (task instanceof CronTask) {
            return new CronTrigger(((CronTask) task).getCron());
        }
        // 周期任务
        else if (task instanceof PeriodTask) {
            PeriodTask periodTask = (PeriodTask) task;
            return new PeriodicTrigger(periodTask.getPeriod(), periodTask.getTimeUnit());
        }
        throw new IllegalArgumentException("任务类型不支持");
    }
}
