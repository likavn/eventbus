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
package com.github.likavn.eventbus.core;

import com.github.likavn.eventbus.core.constant.BusConstant;
import com.github.likavn.eventbus.core.support.task.Task;
import com.github.likavn.eventbus.core.support.task.Timer;
import com.github.likavn.eventbus.core.utils.Assert;
import com.github.likavn.eventbus.core.utils.NamedThreadFactory;
import com.github.likavn.eventbus.core.utils.WaitThreadPoolExecutor;
import lombok.Getter;

import java.util.Map;
import java.util.concurrent.*;

/**
 * 定时任务注册中心，负责管理和调度各种定时任务。
 *
 * @author likavn
 * @date 2024/04/15
 * @since 2.2
 */
public class TaskRegistry {
    /**
     * 使用Timer进行任务定时触发
     */
    @SuppressWarnings("all")
    private final Timer timer = new Timer(false);

    /**
     * 线程池执行器，用于执行任务
     */
    @Getter
    private final WaitThreadPoolExecutor poolExecutor;
    /**
     * 任务映射，存储所有任务
     */
    private final Map<String, Task> taskMap = new ConcurrentHashMap<>();

    /**
     * 构造函数，创建一个带默认线程池的TaskRegistry。
     */
    public TaskRegistry() {
        this.poolExecutor = createDefaultPool();
    }

    /**
     * 构造函数，使用提供的线程池执行器创建TaskRegistry。
     *
     * @param poolExecutor 自定义的线程池执行器
     */
    public TaskRegistry(WaitThreadPoolExecutor poolExecutor) {
        this.poolExecutor = poolExecutor;
    }

    /**
     * 创建任务，添加到任务映射中并开始执行。
     *
     * @param task 要创建的任务
     */
    @SuppressWarnings("all")
    public void createTask(Task task) {
        Assert.isTrue(task.isInitialized(), "任务未初始化！");
        // 确保任务名称唯一
        Assert.isTrue(!taskMap.containsKey(task.getName()), "任务已存在");

        task.setTaskRegistry(this);
        taskMap.put(task.getName(), task);
        // 根据任务的下次执行时间安排任务
        timer.schedule(task, 0, task.nextExecutionTime() - System.currentTimeMillis());
    }

    /**
     * 取消指定的任务。
     *
     * @param task 要取消的任务
     */
    public void removeTask(Task task) {
        if (null == task) {
            return;
        }
        task.cancel();
        taskMap.remove(task.getName());
    }

    /**
     * 刷新指定的任务。
     */
    public void refresh() {
        timer.heapify();
    }

    /**
     * 获取指定名称的任务。
     *
     * @param name 任务名称
     * @return 指定名称的任务
     */
    public Task getTask(String name) {
        return taskMap.get(name);
    }

    /**
     * 创建默认的线程池执行器。
     *
     * @return 默认的线程池执行器
     */
    private WaitThreadPoolExecutor createDefaultPool() {
        return new WaitThreadPoolExecutor(1,
                10, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(10), new NamedThreadFactory(BusConstant.TASK_NAME));
    }
}