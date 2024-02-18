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

import lombok.Data;

/**
 * 任务类，实现了Runnable接口
 *
 * @author likavn
 * @date 2024/1/11
 **/
@Data
public abstract class Task implements Runnable {
    /**
     * 任务名称
     */
    protected String name;
    /**
     * 任务的结果回调
     */
    protected Runnable execute;

    /**
     * 构造方法一：通过任务名称、cron表达式和结果回调创建任务
     *
     * @param name    任务名称
     * @param execute 任务的结果回调
     */
    public Task(String name, Runnable execute) {
        this.name = name;
        this.execute = execute;
    }

    /**
     * 重写run方法，执行任务的结果回调
     */
    @Override
    public void run() {
        if (null != execute) {
            execute.run();
        }
    }
}
