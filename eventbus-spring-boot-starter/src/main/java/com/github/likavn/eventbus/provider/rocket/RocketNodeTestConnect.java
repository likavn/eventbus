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
package com.github.likavn.eventbus.provider.rocket;

import com.github.likavn.eventbus.core.base.NodeTestConnect;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.ClientConfig;
import org.apache.rocketmq.client.impl.MQClientManager;
import org.apache.rocketmq.client.impl.factory.MQClientInstance;
import org.apache.rocketmq.spring.autoconfigure.RocketMQProperties;

/**
 * rocketmq连接状态测试
 *
 * @author likavn
 * @date 2023/5/30
 * @since 2.2
 **/
@Slf4j
public class RocketNodeTestConnect implements NodeTestConnect {
    private final RocketMQProperties rocketMqProperties;
    private MQClientInstance clientFactory;

    public RocketNodeTestConnect(RocketMQProperties rocketMqProperties) {
        this.rocketMqProperties = rocketMqProperties;
    }

    @Override
    @SuppressWarnings("all")
    public synchronized boolean testConnect() {
        try {
            if (null == clientFactory) {
                ClientConfig clientConfig = new ClientConfig();
                clientConfig.setNamesrvAddr(rocketMqProperties.getNameServer());
                // 创建MQClientInstance，这是客户端的核心实例
                clientFactory = MQClientManager.getInstance().getOrCreateMQClientInstance(clientConfig);

                // 启动客户端实例，连接到NameServer
                clientFactory.start();
            }
            // 设置客户端实例的NameServer地址
            clientFactory.getMQClientAPIImpl().getBrokerClusterInfo(1000 * 3L);
            return true;
        } catch (Exception ex) {
            log.error("rocketmq testConnect", ex);
            return false;
        }
    }
}
