package com.github.likavn.eventbus.rabbitmq.support;

import com.rabbitmq.client.ConnectionFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * @author likavn
 * @date 2023/12/21
 **/
public class ConnectPool implements Closeable {
    private List<Connect> connections = new ArrayList<>(1);
    private ConnectionFactory connectionFactory;

    public ConnectPool(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public Connect createConnect() throws IOException, TimeoutException {
        Connect connect = new Connect(connectionFactory);
        connections.add(connect);
        return connect;
    }

    @Override
    public void close() throws IOException {
        for (Connect connect : connections) {
            connect.close();
        }
        connections = new ArrayList<>(1);
    }
}
