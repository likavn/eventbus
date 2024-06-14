package com.github.likavn.eventbus.core.utils;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 执行时校验当前线程池可用
 *
 * @author likavn
 * @date 2024/01/01
 **/
public class WaitThreadPoolExecutor extends ThreadPoolExecutor {
    public WaitThreadPoolExecutor(int corePoolSize,
                                  int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }

    /**
     * 等待ThreadPoolExecutor变为可用状态，然后执行任务。
     *
     * @param r 要执行的任务
     */
    @Override
    public synchronized void execute(Runnable r) {
        // 等待ThreadPoolExecutor变为可用状态
        while (!isShutdown() && !isTerminated()) {
            if (getPoolSize() < getMaximumPoolSize()
                    || getQueue().remainingCapacity() > 0) {
                // 线程池有足够的线程和队列容量，可以提交任务
                super.execute(r);
                break;
            }
        }
    }
}
