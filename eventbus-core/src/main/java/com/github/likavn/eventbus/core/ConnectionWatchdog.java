package com.github.likavn.eventbus.core;

import com.github.likavn.eventbus.core.base.NetLifecycle;
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
    private boolean active = true;

    private final AtomicLong firstLostConnectMillisecond = new AtomicLong(-1);

    private final NodeTestConnect testConnect;

    private final BusConfig.TestConnect properties;

    private final Collection<NetLifecycle> containers;

    public ConnectionWatchdog(NodeTestConnect testConnect,
                              BusConfig.TestConnect testConnectProperties, Collection<NetLifecycle> containers) {
        this.testConnect = testConnect;
        this.properties = testConnectProperties;
        this.containers = containers;
        if (Func.isEmpty(containers)) {
            return;
        }
        try {
            register();
        } catch (Exception e) {
            log.error("ConnectionWatchdog init", e);
        }
        registerScheduler();
        log.info("ConnectionWatchdog init");
    }

    /**
     * 注册连接检测定时任务，校验连接并重新注册或销毁容器
     */
    private void registerScheduler() {
        long loseConnectMaxMilliSecond = 1000L * properties.getLoseConnectMaxMilliSecond();
        ScheduledExecutorService scheduler
                = new ScheduledThreadPoolExecutor(1, new NamedThreadFactory(THREAD_NAME_PREFIX));
        scheduler.scheduleWithFixedDelay(() -> {
            try {
                boolean isConnect = false;
                try {
                    isConnect = testConnect.testConnect();
                } catch (Exception ex) {
                    log.error("ConnectionWatchdog.testConnect", ex);
                }

                if (!active && isConnect) {
                    register();
                    active = true;
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
                    if (active) {
                        destroy();
                    }
                    active = false;
                }
            } catch (Exception ex) {
                log.error("ConnectionWatchdog.registerScheduler", ex);
            }
        }, 0, properties.getPollSecond(), TimeUnit.SECONDS);
    }

    /**
     * 注册所有容器
     */
    private void register() throws Exception {
        // 遍历容器列表
        for (NetLifecycle container : containers) {
            // 注册容器
            container.register();
        }
    }

    /**
     * 销毁所有容器
     */
    private void destroy() throws Exception {
        // 遍历容器列表
        for (NetLifecycle container : containers) {
            // 销毁容器
            container.destroy();
        }
    }

}
