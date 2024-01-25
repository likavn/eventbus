package com.github.likavn.eventbus;

import com.github.likavn.eventbus.core.ConnectionWatchdog;
import com.github.likavn.eventbus.core.base.Lifecycle;
import com.github.likavn.eventbus.core.base.NodeTestConnect;
import com.github.likavn.eventbus.core.metadata.BusConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import java.util.Collection;

/**
 * 启动
 *
 * @author likavn
 * @date 2024/1/17
 **/
@Slf4j
public class BootConnectionWatchdog
        extends ConnectionWatchdog implements ApplicationRunner, DisposableBean {
    public BootConnectionWatchdog(NodeTestConnect testConnect,
                                  BusConfig.TestConnect testConnectProperties,
                                  Collection<Lifecycle> listeners) {
        super(testConnect, testConnectProperties, listeners);
    }

    @Override
    public void run(ApplicationArguments args) {
        super.startup();
    }

    @Override
    public void destroy() {
        super.shutdown();
    }
}
