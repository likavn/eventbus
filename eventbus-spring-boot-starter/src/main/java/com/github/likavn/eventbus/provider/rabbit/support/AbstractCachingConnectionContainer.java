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
package com.github.likavn.eventbus.provider.rabbit.support;

import com.github.likavn.eventbus.core.base.Lifecycle;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.RabbitUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * AbstractCachingConnectionContainer
 *
 * @author likavn
 * @date 2024/1/20
 **/
public abstract class AbstractCachingConnectionContainer implements Lifecycle {
    private final CachingConnectionFactory connectionFactory;
    private Connection connection = null;
    private final List<Channel> channels = Collections.synchronizedList(new ArrayList<>());

    protected AbstractCachingConnectionContainer(CachingConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public synchronized Connection getConnection() {
        if (null == connection) {
            connection = connectionFactory.createConnection();
        }
        return connection;
    }

    public synchronized Channel createChannel() {
        Channel channel = getConnection().createChannel(false);
        channels.add(channel);
        return channel;
    }

    @Override
    public void destroy() {
        if (!channels.isEmpty()) {
            Iterator<Channel> iterator = channels.iterator();
            while (iterator.hasNext()) {
                Channel channel = iterator.next();
                RabbitUtils.setPhysicalCloseRequired(channel, true);
                RabbitUtils.closeChannel(channel);
                iterator.remove();
            }
        }
        // destroy
        RabbitUtils.closeConnection(this.connection);
    }
}
