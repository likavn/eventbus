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

import com.github.likavn.eventbus.core.base.AcquireListeners;
import com.github.likavn.eventbus.core.base.Lifecycle;
import com.github.likavn.eventbus.core.constant.BusConstant;
import com.github.likavn.eventbus.core.exception.EventBusException;
import com.github.likavn.eventbus.core.metadata.BusConfig;
import com.github.likavn.eventbus.core.metadata.support.Listener;
import com.github.likavn.eventbus.core.utils.Func;
import com.github.likavn.eventbus.provider.rabbit.constant.RabbitConstant;
import com.rabbitmq.client.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.RabbitUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AbstractRabbitRegisterContainer
 *
 * @author likavn
 * @date 2024/1/20
 **/
@Slf4j
public abstract class AbstractRabbitRegisterContainer implements AcquireListeners<RabbitListener>, Lifecycle {
    private final CachingConnectionFactory connectionFactory;
    private Connection connection = null;
    private final List<Channel> channels = Collections.synchronizedList(new ArrayList<>());
    protected final BusConfig config;

    protected AbstractRabbitRegisterContainer(CachingConnectionFactory connectionFactory, BusConfig config) {
        this.connectionFactory = connectionFactory;
        this.config = config;
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
    public void register() {
        List<RabbitListener> listeners = getListeners();
        try (Channel channel = getConnection().createChannel(false)) {
            // 根据监听器类型创建交换机
            createExchanges(channel, listeners);
            createQueues(channel, listeners);
            queueBinds(channel, listeners);
            for (RabbitListener listener : listeners) {
                // 对每个监听器，根据其并发级别创建消费者
                Func.pollRun(listener.isRetry() ? listener.getRetryConcurrency() : listener.getConcurrency(), () -> {
                    // 创建消费者的过程可能会抛出IOException
                    try {
                        createConsumer(listener);
                    } catch (IOException e) {
                        log.error("[register createConsumer error] ", e);
                        throw new EventBusException(e);
                    }
                });
            }
        } catch (Exception e) {
            log.error("[register error] ", e);
            // 将捕获到的异常包装为EventBusException并抛出
            throw new EventBusException(e);
        }
    }

    /**
     * 创建消费者并开始消费消息
     * 该方法根据传入的RabbitListener对象配置，创建一个新的信道，绑定队列、交换机和路由键，然后开始消费消息
     * 消费者使用DefaultConsumer类的匿名子类来处理接收到的消息
     *
     * @param listener RabbitListener对象，包含了监听器的相关配置信息，如队列名、交换机名和路由键
     * @throws IOException 如果在创建信道或开始消费过程中发生I/O错误
     */
    private void createConsumer(RabbitListener listener) throws IOException {
        Channel channel = createChannel();
        // 设置通道的消息批量处理大小
        channel.basicQos(config.getMsgBatchSize());
        // 开始消费消息，自动应答设置为false，即需要手动确认消息处理
        channel.basicConsume(listener.getQueue(), false, new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String oldName = Func.reThreadName(BusConstant.THREAD_NAME);
                try {
                    deliver(listener, body);
                    // 手动确认消息处理成功
                    channel.basicAck(envelope.getDeliveryTag(), false);
                } catch (Exception e) {
                    // 记录消息处理失败的日志，并重试或拒绝消息
                    log.error("[Eventbus error] ", e);
                    // 重试消息，不处理其他消息，true表示重新入队
                    channel.basicNack(envelope.getDeliveryTag(), false, true);
                } finally {
                    Thread.currentThread().setName(oldName);
                }
            }
        });
    }

    /**
     * 绑定队列到交换机
     *
     * @param channel   用于声明队列的信道
     * @param listeners RabbitListener注解的列表，包含队列名等信息
     * @throws IOException 如果队列绑定过程中发生I/O错误
     */
    private void queueBinds(Channel channel, List<RabbitListener> listeners) throws IOException {
        for (RabbitListener listener : listeners) {
            // 将队列绑定到交换机，并使用指定的路由键
            channel.queueBind(listener.getQueue(), listener.getExchange(), listener.getRoutingKey());
        }
    }

    /**
     * 创建消息队列
     * 根据RabbitListener注解中的队列名创建队列，确保每个队列名只创建一次队列
     *
     * @param channel   用于声明队列的信道
     * @param listeners RabbitListener注解的列表，包含队列名等信息
     * @throws IOException 如果队列声明过程中发生I/O错误
     */
    private void createQueues(Channel channel, List<RabbitListener> listeners) throws IOException {
        // 提取队列名并去重，因为同一个队列名可能出现在多个RabbitListener注解中
        List<String> queueNames = listeners.stream().map(RabbitListener::getQueue).distinct().collect(Collectors.toList());
        // 遍历队列名列表，为每个队列名声明一个队列
        for (String queueName : queueNames) {
            // 队列声明参数分别是：队列名，是否持久化，是否独占，是否自动删除，和其他属性
            channel.queueDeclare(queueName, true, false, false, null);
        }
    }

    /**
     * 创建交换机
     * 根据监听器列表中的交换机配置，在RabbitMQ中声明对应的交换机
     *
     * @param channel   RabbitMQ的通道，用于声明交换机
     * @param listeners 监听器列表，包含了交换机的配置信息
     * @throws IOException 如果在声明交换机过程中发生I/O错误
     */
    private void createExchanges(Channel channel, List<RabbitListener> listeners) throws IOException {
        List<String> exNames = listeners.stream().map(RabbitListener::getExchange).distinct().collect(Collectors.toList());
        // 获取延时交换机名称
        String delayExName = String.format(RabbitConstant.DELAY_EXCHANGE, config.getServiceId());
        for (String exName : exNames) {
            String type = BuiltinExchangeType.TOPIC.getType();
            Map<String, Object> args = new HashMap<>(4);
            if (exName.equals(delayExName)) {
                // 如果交换机名称包含“delay”，则使用延迟交换机类型，并设置相应的参数
                type = "x-delayed-message";
                args.put("x-delayed-type", "direct");
            }
            // 声明交换机，指定交换机名称、类型、是否持久化、是否自动删除及其他参数
            channel.exchangeDeclare(exName, type, true, false, args);
        }
    }

    /**
     * 消费消息
     *
     * @param listener 消费者
     * @param body     消息体
     */
    protected abstract void deliver(Listener listener, byte[] body);

    @Override
    public synchronized void destroy() {
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
