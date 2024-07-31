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
 * CronTask类继承自Task类，用于实现基于Cron表达式定时执行的任务。
 * 使用CronExpression来解析和计算任务的下一个执行时间。
 *
 * @author likavn
 * @date 2024/04/15
 * @since 2.2
 */
@Slf4j
public class CronTask extends Task {
    private CronExpression cronExpression;

    /**
     * 初始化Cron任务。
     *
     * @param name     任务名称。
     * @param cron     Cron表达式，用于定义任务的执行周期。
     * @param runnable 任务的具体执行逻辑。
     * @throws EventBusException 如果Cron表达式解析失败，则抛出此异常。
     */
    public void initTask(String name, String cron, Runnable runnable) {
        init(name, runnable);
        Assert.isTrue(CronExpression.isValidExpression(cron), "cron表达式不合法");
        try {
            this.cronExpression = new CronExpression(cron);
        } catch (ParseException e) {
            throw new EventBusException(e);
        }
    }

    /**
     * 创建并初始化一个Cron任务。
     *
     * @param name     任务名称。
     * @param cron     Cron表达式。
     * @param runnable 任务执行体。
     * @return 初始化后的CronTask实例。
     */
    public static CronTask create(String name, String cron, Runnable runnable) {
        CronTask task = new CronTask();
        task.initTask(name, cron, runnable);
        return task;
    }

    /**
     * 计算任务的下一个执行时间。
     *
     * @return 下一个执行时间的毫秒数，如果不存在下一个执行时间则返回0。
     */
    @Override
    public long nextExecutionTime() {
        Date next = cronExpression.getNextValidTimeAfter(new Date());
        return next != null ? next.getTime() : 0;
    }
}
