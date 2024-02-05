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

import lombok.Getter;

import java.util.concurrent.TimeUnit;

/**
 * 周期任务
 *
 * @author likavn
 * @date 2024/2/4
 **/
@Getter
public class PeriodTask extends Task {
    /**
     * 最大轮询时间间隔，单位：毫秒
     */
    private static final long MAX_POLL_MILLIS = 1000L * 5;
    /**
     * 任务的周期
     */
    private long period;
    /**
     * 任务的时间单位
     */
    private TimeUnit timeUnit;
    /**
     * 下次任务触发时间
     */
    private long nextTriggerTimeMillis;

    /**
     * 构造方法二：通过任务名称、周期和结果回调创建任务
     *
     * @param name    任务名称
     * @param period  任务的周期
     * @param execute 任务的结果回调
     */
    public PeriodTask(String name, long period, Runnable execute) {
        this(name, period, TimeUnit.MILLISECONDS, execute);
    }

    /**
     * 构造方法三：通过任务名称、周期和时间单位创建任务
     *
     * @param name     任务名称
     * @param period   任务的周期
     * @param timeUnit 任务的时间单位
     * @param execute  任务的结果回调
     */
    public PeriodTask(String name, long period, TimeUnit timeUnit, Runnable execute) {
        super(name, execute);
        this.period = period;
        this.timeUnit = timeUnit;
    }

    /**
     * 重写run方法，执行任务的结果回调
     */
    @Override
    public void run() {
        if (null == execute) {
            return;
        }

        if (System.currentTimeMillis() < nextTriggerTimeMillis) {
            return;
        }

        execute.run();
    }

    /**
     * 设置下次任务触发时间
     *
     * @param nextTriggerTimeMillis 下次任务触发时间
     */
    public void setNextTriggerTimeMillis(long nextTriggerTimeMillis) {
        long l = System.currentTimeMillis();
        if (nextTriggerTimeMillis - l > MAX_POLL_MILLIS) {
            nextTriggerTimeMillis = l + MAX_POLL_MILLIS;
        }
        if (0 < this.nextTriggerTimeMillis && this.nextTriggerTimeMillis < nextTriggerTimeMillis) {
            if (l - this.nextTriggerTimeMillis < 0) {
                return;
            }
        }
        this.nextTriggerTimeMillis = nextTriggerTimeMillis;
    }
}
