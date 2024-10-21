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

import com.github.likavn.eventbus.core.exception.EventBusException;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

/**
 * 分组线程池
 * <p>
 * 多分组任务共享线程池
 *
 * @author likavn
 * @date 2024/01/01
 */
@Slf4j
public class GroupedThreadPoolExecutor {
    /**
     * 线程池中空闲的线程队列，用于存放空闲的线程对象
     */
    private final Queue<WorkerThread> freePools = new ArrayDeque<>();
    /**
     * 线程池中正在使用的线程队列，用于存放正在使用的线程对象
     */
    private final Set<WorkerThread> busyPools = new HashSet<>();
    /**
     * 存储每个任务的状态信息，包括剩余执行次数等
     */
    private final Map<String, State> stateMap = new ConcurrentHashMap<>();
    /**
     * 主锁，用于保护核心线程池和空闲线程队列的并发访问
     */
    private final ReentrantLock mainLock = new ReentrantLock();
    /**
     * 当前核心线程池大小，用于统计核心线程池中正在使用的线程数量
     */
    private final AtomicInteger currentCorePoolSize = new AtomicInteger(0);

    private final int corePoolSize;
    private final long keepAliveTimeMillis;
    private final NamedThreadFactory threadFactory;

    /**
     * 执行前的回调函数，用于在任务执行前对线程进行一些初始化操作，如设置线程名称等
     */
    @Setter
    private Consumer<WorkerThread> beforeConsumer;

    /**
     * 执行后的回调函数，用于在任务执行结束后对线程进行一些收尾工作，如清理线程名称等
     */
    @Setter
    private Consumer<WorkerThread> afterConsumer;

    public GroupedThreadPoolExecutor(int corePoolSize, long keepAliveTimeMillis, NamedThreadFactory threadFactory) {
        this.corePoolSize = corePoolSize;
        this.keepAliveTimeMillis = keepAliveTimeMillis;
        this.threadFactory = threadFactory;
    }

    /**
     * 执行给定的任务并返回任务的执行状态
     * 此方法首先验证任务是否有效，然后在适当的线程上执行任务，并检查任务执行后的剩余次数
     *
     * @param task 要执行的任务对象，不能为空
     * @return 返回任务执行后的剩余次数是否大于0
     */
    public boolean execute(GTask task) {
        // 验证任务是否有效，这是执行任务前的必要检查
        task.isValid();
        // 获取适当的线程来执行任务
        takeThread(task).execute();
        // 检查任务执行后的剩余次数是否大于0，这是判断任务执行状态的重要依据
        return getState(task).getLeftCount() > 0;
    }

    private WorkerThread takeThread(GTask task) {
        State state = blockState(task);
        mainLock.lock();
        WorkerThread workThread = freePools.poll();
        try {
            if (null == workThread) {
                boolean isCore = currentCorePoolSize.get() < corePoolSize;
                workThread = addWorkThread(isCore);
                currentCorePoolSize.incrementAndGet();
            }
            busyPools.add(workThread);
        } catch (Exception e) {
            state.increment();
            throw new EventBusException(e);
        } finally {
            mainLock.unlock();
        }
        workThread.addTask(task);
        return workThread;
    }

    private State blockState(GTask task) {
        State state = getState(task);
        // 按分组进行分段式锁控制
        state.lock();
        try {
            while (state.getLeftCount() < 1) {
                synchronized (state.update) {
                    state.update.wait(1000);
                }
            }
            state.decrement();
        } catch (InterruptedException e) {
            throw new EventBusException(e);
        } finally {
            state.unlock();
        }
        return state;
    }

    public void beforeExecute(WorkerThread worker) {
        if (null != beforeConsumer) {
            beforeConsumer.accept(worker);
        }
    }

    public void afterExecute(WorkerThread worker, Throwable t) {
        //   System.out.println("afterExecute getLock= " + worker.getName());
        mainLock.lock();
        try {
            //   System.out.println("afterExecute locked= " + worker.getName());
            freePools.add(worker);
            busyPools.remove(worker);
            State state = getState(worker.getTask());
            //      System.out.println("afterExecute locked increment bf= " + worker.getName() + "    " + state.getSurplusCount());
            //     System.out.println("afterExecute locked increment af= " + worker.getName() + "    " + state.getSurplusCount());
            state.increment();
        } finally {
            mainLock.unlock();
        }
        if (null != afterConsumer) {
            afterConsumer.accept(worker);
        }
    }

    /**
     * 创建一个新的工作线程并加入到线程池中
     *
     * @param core 指示是否为核心线程，核心线程在任务完成后不会立即被回收
     * @return 创建的WorkerThread对象
     */
    private WorkerThread addWorkThread(boolean core) {
        return new WorkerThread(threadFactory, core);
    }

    /**
     * 当工作线程退出时的处理方法
     * 主要用于从空闲池中移除线程，并减小核心线程池大小
     *
     * @param thread 退出的工作线程
     */
    private void workerThreadExit(WorkerThread thread) {
        mainLock.lock();
        try {
            System.out.println("准备销毁线程..." + thread.getName() + " ,freePools size=" + freePools.size() + ",busyPools size=" + busyPools.size() + ",currentCorePoolSize=" + currentCorePoolSize.get());
            // 从空闲池中移除指定的工作线程
            boolean removed = freePools.removeIf(t -> t.equals(thread));
            // 如果线程被成功移除，则减小核心线程池大小并中断线程
            if (removed) {
                thread.interrupt();
                currentCorePoolSize.decrementAndGet();
                threadFactory.decrement();
            }
        } finally {
            mainLock.unlock();
        }
    }

    /**
     * 清空所有线程池
     * 主要包括空闲池和忙碌池，并清除状态映射
     */
    public void clear() {
        mainLock.lock();
        try {
            // 清空空闲池和忙碌池中的所有线程，并清除状态映射
            clearThreadsIterator(freePools.iterator());
            clearThreadsIterator(busyPools.iterator());
            stateMap.clear();
            currentCorePoolSize.set(0);
            threadFactory.getThreadNumber().set(1);
        } finally {
            mainLock.unlock();
        }
    }

    /**
     * 中断并移除迭代器中的所有线程
     *
     * @param iterator 需要清理的线程迭代器
     */
    private void clearThreadsIterator(Iterator<WorkerThread> iterator) {
        while (iterator.hasNext()) {
            WorkerThread thread = iterator.next();
            thread.interrupt();
            iterator.remove();
        }
    }

    /**
     * 根据任务获取其对应的状态
     * 如果状态不存在，则创建一个新的状态并将其与任务关联
     *
     * @param task 任务对象，包含任务的名称和并发度
     * @return 返回与任务关联的状态对象
     */
    private State getState(GTask task) {
        // 从状态映射中获取与任务名称关联的状态
        State state = stateMap.get(task.getName());
        if (null == state) {
            // 如果没有找到关联的状态，创建一个新的状态对象
            state = new State();
            state.setGroupName(task.getName());
            state.setConcurrency(task.getConcurrency());
            state.setLeftCount(task.getConcurrency());
            // 使用 putIfAbsent 方法实现线程安全的插入操作
            state = stateMap.putIfAbsent(task.getName(), state);
            if (state == null) {
                state = stateMap.get(task.getName());
            }
        }
        return state;
    }

    /**
     * GTask类表示一个可运行的任务，它包含任务的详细信息和运行目标
     * 该类实现了Runnable接口，因此可以被线程执行
     * 使用了@Data，@AllArgsConstructor和@NoArgsConstructor注解来自动生成getter和setter方法，以及构造函数
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GTask implements Runnable {
        // 任务名称，用于标识任务
        private String name;
        // 任务并发数，表示该任务可以同时运行的线程数
        private int concurrency;
        // 任务数据，可以是任何类型的对象，用于任务的运行逻辑中
        private Object data;
        // 任务运行目标，真正的任务执行逻辑
        private Runnable target;

        /**
         * 设置任务运行目标
         *
         * @param target 任务运行目标，一个实现了Runnable接口的对象
         * @return 当前GTask对象，方便链式调用
         */
        public GTask target(Runnable target) {
            this.target = target;
            return this;
        }

        /**
         * 执行任务
         * 在运行时，会检查任务目标是否为空，如果不为空则执行之
         */
        @Override
        public void run() {
            if (target != null) {
                target.run();
            }
        }

        /**
         * 验证任务信息是否有效
         * 检查任务名称、并发数和任务目标是否符合要求
         */
        public void isValid() {
            // 验证任务名称不为空
            Assert.isTrue(null != name && !name.isEmpty(), "name must not null");
            // 验证并发数大于0
            Assert.isTrue(concurrency > 0, "concurrency must > 0");
            // 验证任务目标不为空
            Assert.isTrue(null != target, "target must not null");
        }
    }

    /**
     * State类用于表示某个状态或资源的使用情况，提供并发访问的机制。
     * 该类通过注解自动生成getter和setter方法（@Data），无参构造函数（@NoArgsConstructor），
     * 以及包含所有成员变量的构造函数（@AllArgsConstructor）。
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class State {
        // 定义一个锁对象，用于控制并发访问，确保线程安全。
        private final ReentrantLock lock = new ReentrantLock();

        // groupName表示资源所属的组名。
        private String groupName;

        // concurrency表示并发数，即允许同时访问资源的线程数量。
        private int concurrency;

        // leftCount用于记录剩余的可用资源数量，采用AtomicInteger保证线程安全。
        private int leftCount;

        final Object update = new Object();

        /**
         * 获取当前剩余可用资源的数量。
         *
         * @return 剩余资源数量。
         */
        public int leftCount() {
            synchronized (update) {
                return leftCount;
            }
        }

        /**
         * 增加一个可用资源，采用原子操作保证线程安全。
         */
        public void increment() {
            synchronized (update) {
                leftCount++;
                update.notify();
                //   System.out.println("返回线程increment name=" + getGroupName() + "  " + leftCount());
            }
        }

        /**
         * 减少一个可用资源，采用原子操作保证线程安全。
         */
        public void decrement() {
            synchronized (update) {
                leftCount--;
                //  System.out.println("占用线程increment name=" + getGroupName() + "  " + leftCount());
            }
        }

        public void lock() {
            lock.lock();
        }

        public void unlock() {
            lock.unlock();
        }
    }

    /**
     * WorkerThread 类继承自 Thread，用于处理 GTask 任务的线程
     * 它可以根据是否为核心线程来决定其生命周期
     * <p>
     * 注：此处不能重写hashCode和equals方法，否则会导致线程池的线程管理机制失效
     */
    @EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
    public class WorkerThread extends Thread {
        @EqualsAndHashCode.Include
        private final String id;

        // 标识该线程是否为核心线程
        private final boolean core;

        // 当前线程正在处理的任务
        @Getter
        private GTask task;

        // 记录上次运行任务的时间，用于判断线程是否超时
        private long lastRunTimeMillis;

        // 监控对象，用于线程同步
        private final Object monitor = new Object();

        /**
         * 构造函数，初始化 WorkerThread
         *
         * @param factory 线程工厂，用于生成线程名
         * @param core    是否为核心线程
         */
        public WorkerThread(NamedThreadFactory factory, boolean core) {
            super(factory.getPrefix() + factory.increment());
            this.id = getName();
            this.core = core;
            this.lastRunTimeMillis = System.currentTimeMillis();
            start();
        }

        /**
         * 为线程添加任务
         *
         * @param task 待执行的任务
         */
        private void addTask(GTask task) {
            this.task = task;
        }

        /**
         * 执行当前线程的任务
         */
        public void execute() {
            // 确保任务不为空
            Assert.isTrue(null != task, "task must not null");
            synchronized (monitor) {
                monitor.notify();
            }
        }

        /**
         * 线程的运行方法，不断循环直到被中断
         */
        @Override
        public void run() {
            while (!interrupted()) {
                try {
                    runTask();
                    synchronized (monitor) {
                        monitor.wait(keepAliveTimeMillis);
                    }
                } catch (InterruptedException ignored) {
                    interrupt();
                }
            }
            // 线程销毁时打印信息
            System.out.println("线程已销毁=" + getName());
        }

        /**
         * 运行任务的方法，处理任务并记录运行时间
         */
        private void runTask() {
            if (null == task) {
                if (!core) {
                    exit();
                }
                return;
            }
            Exception throwable = null;
            try {
                beforeExecute(this);
                task.run();
                this.lastRunTimeMillis = System.currentTimeMillis();
            } catch (Exception e) {
                throwable = e;
            } finally {
                afterExecute(this, throwable);
                this.task = null;
            }
        }

        /**
         * 判断是否退出线程的方法，根据上次运行时间和保持时间对比决定
         */
        private void exit() {
            if (System.currentTimeMillis() - this.lastRunTimeMillis < keepAliveTimeMillis) {
                return;
            }
            workerThreadExit(this);
        }
    }
}
