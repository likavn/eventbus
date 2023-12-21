package com.github.likavn.eventbus.rabbitmq.support;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * @author likavn
 * @date 2023/12/21
 **/
@Slf4j
@Data
public class Connect implements Closeable {
    private Connection connection;
    private List<Channel> channels;

    public Connect(Connection connection, List<Channel> channels) {
        this.connection = connection;
        this.channels = channels;
    }

    public Connect(ConnectionFactory connectionFactory) throws IOException, TimeoutException {
        this.connection = connectionFactory.newConnection();
    }

    public Channel createChannel() throws IOException {
        Channel channel = connection.createChannel();
        if (null == channels) {
            channels = new ArrayList<>(1);
            channels.add(channel);
        }
        return channel;
    }

    @Override
    public void close() throws IOException {
        if (null != channels && channels.size() > 0) {
            for (Channel channel : channels) {
                try {
                    channel.close();
                } catch (TimeoutException e) {
                    log.error("close channel TimeoutException", e);
                }
            }
        }
        if (null != connection) {
            connection.close();
        }
    }
}
