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

import com.github.likavn.eventbus.core.exception.EventBusException;
import com.github.likavn.eventbus.core.utils.Assert;
import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.util.Date;

/**
 * 基于cron表达式的任务类，继承自Task类。
 * 用于通过cron表达式定义任务的执行周期。
 *
 * @author likavn
 * @date 2024/04/15
 * @since 2.2
 */
@Slf4j
public class CronTask extends Task {
    /**
     * 任务的cron表达式。
     * 用于定义任务的执行时间表。
     */
    private String cron;

    private CronExpression cronExpression;

    /**
     * CronTask构造函数。
     *
     * @param name     任务名称。
     * @param cron     任务的cron表达式。必须是合法的cron表达式。
     * @param runnable 任务要执行的操作。
     * @throws IllegalArgumentException 如果cron表达式不合法，则抛出此异常。
     */
    public void initTask(String name, String cron, Runnable runnable) {
        init(name, runnable);
        Assert.isTrue(CronExpression.isValidExpression(cron), "cron表达式不合法");
        this.cron = cron;
        try {
            this.cronExpression = new CronExpression(this.cron);
        } catch (ParseException e) {
            throw new EventBusException(e);
        }
    }

    public static CronTask create(String name, String cron, Runnable runnable) {
        CronTask task = new CronTask();
        task.initTask(name, cron, runnable);
        return task;
    }

    /**
     * 计算并返回任务的下一次执行时间。
     *
     * @return 返回下一次执行时间的毫秒数。如果无法确定下一次执行时间，则返回0。
     */
    @Override
    public long nextExecutionTime() {
        Date next = cronExpression.getNextValidTimeAfter(new Date());
        return next != null ? next.getTime() : 0;
    }
}

