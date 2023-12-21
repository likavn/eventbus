package com.github.likavn.eventbus.core;

import com.github.likavn.eventbus.core.base.MsgListenerContainer;
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
 * @date 2023/5/30
 **/
@Slf4j
public class ConnectionWatchdog {

    private boolean active = true;

    private final AtomicLong firstLostConnectMillisecond = new AtomicLong(-1);

    private final NodeTestConnect testConnect;

    private final BusConfig.TestConnect properties;

    private final Collection<MsgListenerContainer> containers;

    public ConnectionWatchdog(NodeTestConnect testConnect,
                              BusConfig.TestConnect testConnectProperties, Collection<MsgListenerContainer> containers) {
        this.testConnect = testConnect;
        this.properties = testConnectProperties;
        this.containers = containers;
        if (Func.isEmpty(containers)) {
            return;
        }
        registerScheduler();
    }

    /**
     * 注册连接检测定时任务，校验连接并重新注册或销毁容器
     */
    private void registerScheduler() {
        long loseConnectMaxMilliSecond = 1000L * properties.getLoseConnectMaxMilliSecond();
        ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(
                1,
                new NamedThreadFactory("notify-connectionWatchdog-pool-"));
        scheduler.scheduleWithFixedDelay(() -> {
            boolean isConnect = false;
            try {
                isConnect = testConnect.testConnect();
            } catch (Exception ex) {
                log.error("ConnectionWatchdog.testConnect", ex);
            }
            log.debug("isConnect... {}", isConnect);
            if (!active && isConnect) {
                register();
                active = true;
            }

            if (isConnect) {
                firstLostConnectMillisecond.set(-1);
            }
            // 丢失连接+1
            else if (firstLostConnectMillisecond.get() == -1) {
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
        }, properties.getPollSecond(), properties.getPollSecond(), TimeUnit.SECONDS);
    }

    private void register() {
        containers.forEach(MsgListenerContainer::register);
    }

    private void destroy() {
        containers.forEach(MsgListenerContainer::destroy);
    }

}
