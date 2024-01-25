package com.github.likavn.eventbus.core;

import com.github.likavn.eventbus.core.base.Lifecycle;
import com.github.likavn.eventbus.core.base.MsgListenerContainer;
import com.github.likavn.eventbus.core.base.NodeTestConnect;
import com.github.likavn.eventbus.core.metadata.BusConfig;
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
public class ConnectionWatchdog extends MsgListenerContainer {
    private static final String THREAD_NAME_PREFIX = "eventbus-connectionWatchdog-pool-";
    private volatile boolean connect = false;
    private final AtomicLong firstLostConnectMillisecond = new AtomicLong(-1);
    private final NodeTestConnect testConnect;
    private final BusConfig.TestConnect properties;
    private ScheduledExecutorService scheduler;

    public ConnectionWatchdog(NodeTestConnect testConnect,
                              BusConfig.TestConnect testConnectProperties, Collection<Lifecycle> listeners) {
        super(listeners);
        this.testConnect = testConnect;
        this.properties = testConnectProperties;
    }

    @Override
    public void startup() {
        // 启动监听器
        super.startup();

        // 创建连接检测定时任务
        createTask();
    }

    /**
     * 注册连接检测定时任务
     */
    private synchronized void createTask() {
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
        try {
            boolean isConnect = false;
            try {
                isConnect = testConnect.testConnect();
            } catch (Exception ex) {
                log.error("testConnect fail", ex);
            }

            if (!connect && isConnect) {
                if (!active) {
                    registerListeners();
                }
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
                    && System.currentTimeMillis() - firstLostConnectMillisecond.get() >= 1000L * properties.getLoseConnectMaxMilliSecond()) {
                if (connect) {
                    destroyListeners();
                }
                connect = false;
            }
        } catch (Exception ex) {
            log.error("pollTestConnectTask", ex);
        }
    }
}
