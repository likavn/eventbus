package com.github.likavn.eventbus.schedule;

import com.github.likavn.eventbus.core.utils.Assert;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.util.StringUtils;

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
        return StringUtils.hasText(task.getCron()) ? new CronTrigger(task.getCron()) : new PeriodicTrigger(task.getPeriod(), task.getTimeUnit());
    }
}
