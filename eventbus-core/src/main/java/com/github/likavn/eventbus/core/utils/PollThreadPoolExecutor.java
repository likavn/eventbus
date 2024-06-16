package com.github.likavn.eventbus.core.utils;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 固定线程池的轮询任务
 *
 * @author likavn
 * @date 2024/01/01
 **/
public class PollThreadPoolExecutor extends WaitThreadPoolExecutor {

    private final AtomicBoolean cancel = new AtomicBoolean(false);


    public PollThreadPoolExecutor(int corePoolSize,
                                  int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        if (cancel.get()) {
            return;
        }
        execute(r);
    }

    /**
     * 启动某个过程或任务。
     * 本方法用于初始化一个用于取消操作的标志位，设置其为false，表示任务或过程尚未被取消。
     * 这是一个重要的初始化步骤，用于确保任务在开始时处于可执行状态。
     *
     * @see java.util.concurrent.atomic.AtomicBoolean
     */
    public void start(){
        cancel.set(false);
    }

    public void cancel() {
        cancel.set(true);
        super.purge();
    }
}
