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
package com.github.likavn.eventbus;

import com.github.likavn.eventbus.core.TaskRegistry;
import com.github.likavn.eventbus.core.support.task.CronTask;
import com.github.likavn.eventbus.core.support.task.PeriodTask;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * 测试定时任务
 *
 * @author likavn
 * @date 2024/4/24
 */
@SuppressWarnings("all")
public class TaskRegistryTest {
    /**
     * 演示创建和注册CronTask。
     */
    @Test
    public void taskRegistry() {
        TaskRegistry taskRegistry = new TaskRegistry();

        CronTask cronTask = CronTask.create("test1", "0,35 * * * * ?", () -> {
            System.out.println("CronTask=" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + ",thread=" + Thread.currentThread().getName());
        });
        taskRegistry.createTask(cronTask);
//
//        PeriodTask periodTask = new PeriodTask("test", 10, TimeUnit.SECONDS, () -> {
//            System.out.println("PeriodTask=" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + ",thread=" + Thread.currentThread().getName());
//        });
//        taskRegistry.createTask(periodTask);
    }

    public static void main(String[] args) throws InterruptedException {
        TaskRegistry taskRegistry = new TaskRegistry();

        CronTask cronTask = CronTask.create("test1", "0/10 * * * * ? ", () -> {
            System.out.println("CronTask=" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + ",thread=" + Thread.currentThread().getName());
        });
        taskRegistry.createTask(cronTask);

        PeriodTask periodTask = PeriodTask.create("test", 10, TimeUnit.SECONDS, () -> {
            System.out.println("PeriodTask=" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + ",thread=" + Thread.currentThread().getName());
        });
//        taskRegistry.createTask(periodTask);
//        Thread.sleep(20 * 1000);
//        periodTask.cancel();
//        Thread.sleep(20 * 1000);
//        periodTask.start();
    }

}
