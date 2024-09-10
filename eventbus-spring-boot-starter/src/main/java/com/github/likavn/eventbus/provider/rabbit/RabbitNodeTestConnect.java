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
package com.github.likavn.eventbus.provider.rabbit;

import com.github.likavn.eventbus.core.base.NodeTestConnect;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.Connection;

/**
 * rabbitMq连接状态测试
 *
 * @author likavn
 * @date 2023/5/30
 **/
@Slf4j
public class RabbitNodeTestConnect implements NodeTestConnect {
    private final CachingConnectionFactory connectionFactory;
    private Connection connection;

    public RabbitNodeTestConnect(CachingConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public synchronized boolean testConnect() {
        try {
            if (null == connection) {
                connection = connectionFactory.createConnection();
            }
            return connection.isOpen();
        } catch (Exception ex) {
            log.error("rabbitMq testConnect", ex);
            return false;
        }
    }
}
