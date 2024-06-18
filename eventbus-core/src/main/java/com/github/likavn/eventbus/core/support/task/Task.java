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
package com.github.likavn.eventbus.core.support.task;

import com.github.likavn.eventbus.core.TaskRegistry;
import com.github.likavn.eventbus.core.utils.WaitThreadPoolExecutor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.TimerTask;

/**
 * 一个抽象的定时任务类，继承自TimerTask，并且包含了任务名称、任务执行逻辑、以及任务调度的一些逻辑。
 * 允许子类定制任务的执行周期。
 *
 * @author likavn
 * @date 2024/04/15
 * @since 2.2
 */
@Slf4j
public abstract class Task extends TimerTask {
    /**
     * 任务名称
     */
    @Getter
    private String name;

    private Runnable runnable;

    /**
     * 下次执行任务的时间戳
     */
    @SuppressWarnings("all")
    long nextExecutionTime;

    /**
     * 上次任务执行的时间
     */
    protected Date lastExecutionTime;

    /**
     * 任务注册
     */
    TaskRegistry taskRegistry;
    /**
     * 线程池执行器，用于执行任务
     */
    private WaitThreadPoolExecutor poolExecutor;

    @Getter
    private boolean initialized = false;

    /**
     * Task类的构造函数。
     *
     * @param name     任务的名称。
     * @param runnable 任务的执行逻辑。
     */
    protected void init(String name, Runnable runnable) {
        this.name = name;
        this.runnable = runnable;
        this.nextExecutionTime = System.currentTimeMillis();
        this.initialized = true;
    }

    /**
     * 重写run方法，以实现任务的执行逻辑。
     * 主要逻辑包括：检查是否到了任务执行的时间，如果到了则计算下一次执行时间并提交任务到线程池执行。
     */
    @Override
    public void run() {
        this.lastExecutionTime = new Date();
        try {
            if (System.currentTimeMillis() < this.nextExecutionTime) {
                return;
            }
            poolExecutor.execute(runnable);
            this.nextExecutionTime = nextExecutionTime();
        } catch (Exception e) {
            log.error("task run error", e);
        }
    }

    /**
     * 设置任务注册器。
     *
     * @param taskRegistry registry
     */
    public void setTaskRegistry(TaskRegistry taskRegistry) {
        this.poolExecutor = taskRegistry.getPoolExecutor();
        this.taskRegistry = taskRegistry;
    }

    /**
     * 设置下次任务触发时间。
     * 如果传入的时间早于当前设定的下次执行时间且这个时间差小于当前时间，则不进行设置。
     *
     * @param nextExecutionTime 下次任务触发时间
     */
    public void setNextExecutionTime(long nextExecutionTime) {
        if (0 < this.nextExecutionTime && this.nextExecutionTime < nextExecutionTime) {
            this.nextExecutionTime = nextExecutionTime;
        }
    }

    /**
     * 计算并返回下一次执行的时间。
     * 该方法为抽象方法，需要在子类中具体实现，以根据特定逻辑计算下一次任务的执行时间。
     *
     * @return 返回一个long类型的值，代表下一次执行的具体时间（通常为毫秒或纳秒精度的时间戳）。
     */
    public abstract long nextExecutionTime();
}

