package com.github.likavn.eventbus.rabbitmq.support.pool;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

/**
 * Channel pool factory
 *
 * @author likavn
 * @date 2024/01/01
 **/
public class ChannelPooledObjectFactory extends BasePooledObjectFactory<Channel> {

    private final Connection connection;

    public ChannelPooledObjectFactory(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Channel create() throws Exception {
        return connection.createChannel();
    }

    @Override
    public PooledObject<Channel> wrap(Channel channel) {
        // 使用默认池化对象包装ComplexObject
        return new DefaultPooledObject(channel);
    }
}
