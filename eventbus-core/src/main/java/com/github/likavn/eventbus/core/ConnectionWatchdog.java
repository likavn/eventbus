package com.github.likavn.eventbus.core;

import com.github.likavn.eventbus.core.base.Lifecycle;
import com.github.likavn.eventbus.core.base.NodeTestConnect;
import com.github.likavn.eventbus.core.exception.EventBusException;
import com.github.likavn.eventbus.core.metadata.BusConfig;
import com.github.likavn.eventbus.core.utils.Func;
import com.github.likavn.eventbus.core.utils.NamedThreadFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 检测连接状态
 *
 * @author likavn
 * @date 2024/01/01
 **/
@Slf4j
public class ConnectionWatchdog {
    private static final String THREAD_NAME_PREFIX = "eventbus-connectionWatchdog-pool-";
    private volatile boolean active = false;
    private volatile boolean connect = false;
    private final AtomicLong firstLostConnectMillisecond = new AtomicLong(-1);
    private final NodeTestConnect testConnect;
    private final BusConfig.TestConnect properties;
    private final Collection<Lifecycle> components;
    private ScheduledExecutorService scheduler;

    public ConnectionWatchdog(NodeTestConnect testConnect,
                              BusConfig.TestConnect testConnectProperties, Collection<Lifecycle> components) {
        this.testConnect = testConnect;
        this.properties = testConnectProperties;
        this.components = components;
    }

    /**
     * 启动检测状态
     */
    public void startup() {
        if (Func.isEmpty(components)) {
            return;
        }
        try {
            this.active = true;
            register();
            this.connect = true;
        } catch (Exception e) {
            log.error("eventbus startup", e);
            throw new EventBusException(e);
        }
        // 启动定时任务
        startTask();
    }

    /**
     * 停止检测状态
     */
    public void shutdown() {
        try {
            this.active = false;
            destroy();
        } catch (Exception e) {
            log.error("eventbus shutdown", e);
            throw new EventBusException(e);
        }
    }

    /**
     * 获取当前监听组件状态
     */
    public boolean isActive() {
        return active;
    }

    /**
     * 注册连接检测定时任务
     */
    private void startTask() {
        if (null != scheduler) {
            return;
        }
        scheduler = new ScheduledThreadPoolExecutor(1, new NamedThreadFactory(THREAD_NAME_PREFIX));
        scheduler.scheduleWithFixedDelay(this::pollTestConnectTask, 0, properties.getPollSecond(), TimeUnit.SECONDS);
    }

    /**
     * 检测连接状态
     */
    private void pollTestConnectTask() {
        long loseConnectMaxMilliSecond = 1000L * properties.getLoseConnectMaxMilliSecond();
        try {
            boolean isConnect = false;
            try {
                isConnect = testConnect.testConnect();
            } catch (Exception ex) {
                log.error("testConnect fail", ex);
            }

            if (!connect && isConnect) {
                register();
                connect = true;
            }

            if (isConnect) {
                firstLostConnectMillisecond.set(-1);
            }
            // 丢失连接+1
            else if (firstLostConnectMillisecond.get() == -1) {
                log.warn("lost connection...");
                firstLostConnectMillisecond.set(System.currentTimeMillis());
            }

            // 丢失超过固定阀值，则销毁容器
            if (firstLostConnectMillisecond.get() != -1
                    && System.currentTimeMillis() - firstLostConnectMillisecond.get() >= loseConnectMaxMilliSecond) {
                if (connect) {
                    destroy();
                }
                connect = false;
            }
        } catch (Exception ex) {
            log.error("pollTestConnectTask", ex);
        }
    }

    /**
     * 注册所有监听组件
     */
    private void register() throws Exception {
        if (!this.active) {
            return;
        }
        // 遍历组件列表
        for (Lifecycle component : components) {
            // 注册组件
            component.register();
        }
        log.info("Eventbus register success");
    }

    /**
     * 销毁所有监听组件
     */
    private void destroy() throws Exception {
        // 遍历组件列表
        for (Lifecycle component : components) {
            // 销毁组件
            component.destroy();
        }
        log.info("Eventbus destroy success");
    }
}
