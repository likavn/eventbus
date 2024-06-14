package com.github.likavn.eventbus.core.utils;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * 固定线程池的轮询任务
 *
 * @author likavn
 * @date 2024/01/01
 **/
public class PollThreadPoolExecutor extends WaitThreadPoolExecutor {
    public PollThreadPoolExecutor(int corePoolSize,
                                  int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        execute(r);
    }

}
