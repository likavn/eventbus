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

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * 周期性任务类，继承自Task类。用于执行具有固定周期的任务。
 *
 * @author likavn
 * @date 2024/04/15
 * @since 2.2
 */
public class PeriodTask extends Task {
    /**
     * 任务的执行周期。表示两次执行之间的时间间隔。
     */
    private long pollInterval;
    /**
     * 任务的周期时间单位。定义了pollInterval的时间单位。
     */
    private TimeUnit timeUnit;

    /**
     * 构造函数，初始化周期性任务。
     *
     * @param name         任务名称。
     * @param pollInterval 任务的执行周期，单位为timeUnit。
     * @param timeUnit     任务的周期时间单位。
     * @param runnable     任务要执行的代码块。
     */
    public void initTask(String name, long pollInterval, TimeUnit timeUnit, Runnable runnable) {
        init(name, runnable);
        this.pollInterval = pollInterval;
        this.timeUnit = timeUnit;
    }

    public static PeriodTask create(String name, long pollInterval, Runnable runnable) {
        return create(name, pollInterval, TimeUnit.MILLISECONDS, runnable);
    }

    public static PeriodTask create(String name, long pollInterval, TimeUnit timeUnit, Runnable runnable) {
        PeriodTask task = new PeriodTask();
        task.initTask(name, pollInterval, timeUnit, runnable);
        return task;
    }

    /**
     * 计算并返回下一次任务执行的时间。
     * 初始执行时间基于构造时的时间，之后每次执行将基于上一次执行的时间。
     *
     * @return 下一次任务执行的绝对时间（毫秒数）。
     */
    @Override
    public long nextExecutionTime() {
        if (null == lastExecutionTime) {
            lastExecutionTime = new Date();
        }
        // 计算下一次执行时间，基于任务的周期和上次执行时间。
        return lastExecutionTime.getTime() + (pollInterval * timeUnit.toMillis(1L));
    }
}
