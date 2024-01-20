package com.github.likavn.eventbus.core;

import com.github.likavn.eventbus.core.base.Lifecycle;
import com.github.likavn.eventbus.core.base.NodeTestConnect;
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

    public ConnectionWatchdog(NodeTestConnect testConnect,
                              BusConfig.TestConnect testConnectProperties, Collection<Lifecycle> components) {
        this.testConnect = testConnect;
        this.properties = testConnectProperties;
        this.components = components;
    }

    /**
     * 启动检测连接状态
     */
    public void startup() {
        if (Func.isEmpty(components)) {
            return;
        }
        try {
            register();
        } catch (Exception e) {
            log.error("ConnectionWatchdog init", e);
        }
        registerScheduler();
    }

    /**
     * 注册连接检测定时任务，校验连接并重新注册或销毁容器
     */
    private void registerScheduler() {
        ScheduledExecutorService scheduler
                = new ScheduledThreadPoolExecutor(1, new NamedThreadFactory(THREAD_NAME_PREFIX));
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
                log.error("ConnectionWatchdog.testConnect", ex);
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
            log.error("ConnectionWatchdog.registerScheduler", ex);
        }
    }

    /**
     * 注册所有组件
     */
    public void register() throws Exception {
        // 遍历组件列表
        for (Lifecycle component : components) {
            // 注册组件
            component.register();
        }
        active = true;
    }

    /**
     * 销毁所有组件
     */
    public void destroy() throws Exception {
        // 遍历组件列表
        for (Lifecycle component : components) {
            // 销毁组件
            component.destroy();
        }
        active = false;
    }

    public boolean isActive() {
        return active;
    }

}
