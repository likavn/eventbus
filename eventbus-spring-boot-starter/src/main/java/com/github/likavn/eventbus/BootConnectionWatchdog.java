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
