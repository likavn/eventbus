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
import com.github.likavn.eventbus.core.utils.Assert;
import com.github.likavn.eventbus.core.utils.NamedThreadFactory;

import java.util.Map;
import java.util.Timer;
import java.util.concurrent.*;

/**
 * 定时任务注册中心，负责管理和调度各种定时任务。
 *
 * @author likavn
 * @date 2024/04/15
 * @since 2.2
 */
public class TaskRegistry {
    // 使用Timer进行任务定时触发
    private final Timer timer = new Timer(false);

    // 线程池执行器，用于执行任务
    private final TaskThreadPoolExecutor poolExecutor;
    // 任务映射，存储所有任务
    private final Map<String, Task> taskMap = new ConcurrentHashMap<>();

    /**
     * 构造函数，创建一个带默认线程池的TaskRegistry。
     */
    public TaskRegistry() {
        this.poolExecutor = TaskThreadPoolExecutor.create();
    }

    /**
     * 构造函数，使用提供的线程池执行器创建TaskRegistry。
     *
     * @param poolExecutor 自定义的线程池执行器
     */
    public TaskRegistry(TaskThreadPoolExecutor poolExecutor) {
        this.poolExecutor = poolExecutor;
    }

    /**
     * 创建任务，添加到任务映射中并开始执行。
     *
     * @param task 要创建的任务
     */
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
     * 获取指定名称的任务。
     *
     * @param name 任务名称
     * @return 指定名称的任务
     */
    public Task getTask(String name) {
        return taskMap.get(name);
    }

    public TaskThreadPoolExecutor getPoolExecutor() {
        return poolExecutor;
    }

    /**
     * 自定义的线程池执行器类，用于执行定时任务。
     */
    public static class TaskThreadPoolExecutor extends ThreadPoolExecutor {
        // 线程池的核心线程数
        private static final int CORE_POOL_SIZE = 1;
        // 线程池的最大线程数
        private static final int MAXIMUM_POOL_SIZE = 10;
        // 空闲线程的存活时间
        private static final long KEEP_ALIVE_TIME = 10L;
        // 时间单位
        private static final TimeUnit UNIT = TimeUnit.SECONDS;
        // 任务队列
        private static final BlockingQueue<Runnable> QUEUE = new LinkedBlockingQueue<>(5);

        /**
         * 构造函数，初始化线程池执行器。
         */
        public TaskThreadPoolExecutor(int corePoolSize,
                                      int maximumPoolSize,
                                      long keepAliveTime,
                                      TimeUnit unit, BlockingQueue<Runnable> workQueue) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, new NamedThreadFactory(BusConstant.TASK_NAME));
        }

        /**
         * 创建并返回一个默认配置的线程池执行器实例。
         *
         * @return 默认配置的线程池执行器实例
         */
        public static TaskThreadPoolExecutor create() {
            return new TaskThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE_TIME, UNIT, QUEUE);
        }

        /**
         * 等待ThreadPoolExecutor变为可用状态，然后执行任务。
         *
         * @param command 要执行的任务
         */
        public synchronized void executeWait(Runnable command) {
            // 等待ThreadPoolExecutor变为可用状态
            while (!isShutdown() && !isTerminated()) {
                if (getPoolSize() < getMaximumPoolSize()
                        || getQueue().remainingCapacity() > 0) {
                    // 线程池有足够的线程和队列容量，可以提交任务
                    super.execute(command);
                    break;
                }
            }
        }
    }
}