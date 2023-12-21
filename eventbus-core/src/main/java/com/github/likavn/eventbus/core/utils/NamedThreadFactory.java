package com.github.likavn.eventbus.core.utils;


import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 带有前缀名称的线程工厂
 *
 * @author likavn
 * @since 2023/01/01
 */
public class NamedThreadFactory implements ThreadFactory {

    /**
     * 线程名前缀
     */
    private final String prefix;

    /**
     * 线程编号
     */
    private final AtomicInteger threadNumber = new AtomicInteger(1);

    /**
     * 创建线程工厂
     *
     * @param prefix 线程名前缀
     */
    public NamedThreadFactory(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public Thread newThread(Runnable r) {
        return new Thread(null, r, prefix + threadNumber.getAndIncrement());
    }
}
