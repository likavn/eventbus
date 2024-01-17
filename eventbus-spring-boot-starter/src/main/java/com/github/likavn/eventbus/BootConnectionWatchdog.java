package com.github.likavn.eventbus;

import com.github.likavn.eventbus.core.ConnectionWatchdog;
import com.github.likavn.eventbus.core.base.Lifecycle;
import com.github.likavn.eventbus.core.base.NodeTestConnect;
import com.github.likavn.eventbus.core.metadata.BusConfig;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import java.util.Collection;

/**
 * 启动
 *
 * @author likavn
 * @date 2024/1/17
 **/
public class BootConnectionWatchdog extends ConnectionWatchdog implements ApplicationRunner {
    public BootConnectionWatchdog(NodeTestConnect testConnect,
                                  BusConfig.TestConnect testConnectProperties, Collection<Lifecycle> components) {
        super(testConnect, testConnectProperties, components);
    }

    @Override
    public void run(ApplicationArguments args) {
        super.startup();
    }
}
