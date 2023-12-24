package com.github.likavn.eventbus.rabbitmq.support.pool;

import com.github.likavn.eventbus.rabbitmq.support.AmqpException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * channel pool
 *
 * @author likavn
 * @date 2023/12/24
 **/
public class ChannelPool {
    private final GenericObjectPool<Channel> internalPool;
    private final ConnectionFactory connectionFactory;
    private final Connection connection;
    private final GenericObjectPoolConfig config;

    public ChannelPool(ConnectionFactory connectionFactory) throws IOException, TimeoutException {
        this.connectionFactory = connectionFactory;
        this.connection = connectionFactory.newConnection();
        this.config = new GenericObjectPoolConfig();
        config.setMaxTotal(50);
        config.setMaxIdle(50);
        config.setMinIdle(1);
        this.internalPool = new GenericObjectPool<>(new ChannelPooledObjectFactory(connection), config);
    }

    public Channel borrowObject() {
        try {
            return this.internalPool.borrowObject();
        } catch (Exception e) {
            throw new AmqpException("获取连接对象失败!", e);
        }
    }

    @PreDestroy
    public void close() {
        try {
            this.internalPool.close();
        } catch (Exception e) {
            throw new AmqpException("销毁对象池失败!", e);
        }
    }

    /**
     * 销毁连接对象
     *
     * @param obj 连接对象
     */
    public void invalidateObject(Channel obj) {
        try {
            this.internalPool.invalidateObject(obj);
        } catch (Exception e) {
            throw new AmqpException("销毁连接对象失败!", e);
        }
    }

    /**
     * 回收连接对象
     *
     * @param obj 连接对象
     */
    public void returnObject(Channel obj) {
        try {
            this.internalPool.returnObject(obj);
        } catch (Exception e) {
            throw new AmqpException("回收连接对象失败!", e);
        }
    }
}
