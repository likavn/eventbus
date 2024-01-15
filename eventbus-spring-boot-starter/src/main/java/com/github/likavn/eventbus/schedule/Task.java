package com.github.likavn.eventbus.schedule;

import lombok.Data;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 任务类，实现了Runnable接口
 *
 * @author likavn
 * @date 2024/1/11
 **/
@Data
public class Task implements Runnable {
    /**
     * 任务名称
     */
    private String name;
    /**
     * 任务的cron表达式
     */
    private String cron;
    /**
     * 任务的周期
     */
    private long period;
    /**
     * 任务的时间单位
     */
    private TimeUnit timeUnit;
    /**
     * 任务的结果回调
     */
    private Runnable result;

    /**
     * 构造方法一：通过任务名称、cron表达式和结果回调创建任务
     *
     * @param name   任务名称
     * @param cron   任务的cron表达式
     * @param result 任务的结果回调
     */
    public Task(String name, String cron, Runnable result) {
        this.name = name;
        this.cron = cron;
        this.result = result;
    }

    /**
     * 构造方法二：通过任务名称、周期和结果回调创建任务
     *
     * @param name   任务名称
     * @param period 任务的周期
     * @param result 任务的结果回调
     */
    public Task(String name, long period, Runnable result) {
        this(name, period, TimeUnit.MILLISECONDS, result);
    }

    /**
     * 构造方法三：通过任务名称、周期和时间单位创建任务
     *
     * @param name     任务名称
     * @param period   任务的周期
     * @param timeUnit 任务的时间单位
     * @param result   任务的结果回调
     */
    public Task(String name, long period, TimeUnit timeUnit, Runnable result) {
        this.name = name;
        this.period = period;
        this.timeUnit = timeUnit;
        this.result = result;
    }

    /**
     * 重写run方法，执行任务的结果回调
     */
    @Override
    public void run() {
        result.run();
    }

    /**
     * 重写equals方法，用于判断两个任务是否相等
     *
     * @param o 待比较的对象
     * @return 若对象相等，则返回true，否则返回false
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Task task = (Task) o;
        return name.equals(task.name);
    }

    /**
     * 重写hashCode方法，用于计算任务的哈希值
     *
     * @return 任务的哈希值
     */
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
