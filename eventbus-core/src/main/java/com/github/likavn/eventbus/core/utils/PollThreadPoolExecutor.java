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
package com.github.likavn.eventbus.core.utils;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * 继承自WaitThreadPoolExecutor的PollThreadPoolExecutor类，用于实现具有轮询功能的固定大小线程池。
 * 该线程池的主要特点是在任务执行前后进行特定的逻辑处理，以支持轮询重试等机制。
 *
 * @author likavn
 * @date 2024/01/01
 */
public class PollThreadPoolExecutor extends WaitThreadPoolExecutor {
    /**
     * 标志线程池是否正在运行，用于控制任务的重新执行
     */
    private volatile boolean running = true;

    /**
     * 构造函数，初始化PollThreadPoolExecutor。
     *
     * @param corePoolSize    核心线程数
     * @param maximumPoolSize 最大线程数
     * @param keepAliveTime   线程的空闲存活时间
     * @param unit            时间单位
     * @param workQueue       工作队列，用于存储待执行的任务
     * @param threadFactory   线程工厂，用于创建线程
     */
    public PollThreadPoolExecutor(int corePoolSize,
                                  int maximumPoolSize,
                                  long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }

    /**
     * 执行任务前，检查线程池是否正在运行。
     * 如果是，则提交任务至线程池执行。
     *
     * @param r 要执行的任务
     */
    @Override
    public synchronized void execute(Runnable r) {
        running = true;
        super.execute(r);
    }

    /**
     * 任务执行后，如果线程池仍在运行，则尝试重新执行任务。
     * 这支持了任务的轮询重试机制。
     *
     * @param r 执行的任务
     * @param t 执行过程中可能抛出的异常
     */
    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        if (running) {
            super.execute(r);
        }
    }

    /**
     * 线程池终止时，将运行标志设置为false。
     * 这用于标记线程池不再接受新的任务。
     */
    @Override
    public void terminated() {
        running = false;
    }
}
