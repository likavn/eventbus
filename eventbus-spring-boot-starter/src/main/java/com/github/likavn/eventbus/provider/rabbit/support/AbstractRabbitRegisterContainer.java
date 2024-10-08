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
import com.github.likavn.eventbus.core.metadata.MsgType;
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

/**
 * AbstractRabbitRegisterContainer
 *
 * @author likavn
 * @date 2024/1/20
 **/
@Slf4j
public abstract class AbstractRabbitRegisterContainer implements AcquireListeners, Lifecycle {
    private final CachingConnectionFactory connectionFactory;
    private Connection connection = null;
    private final List<Channel> channels = Collections.synchronizedList(new ArrayList<>());
    protected final BusConfig config;
    private String exchangeName = null;
    private boolean applicationStarted = false;
    private final MsgType msgType;

    protected AbstractRabbitRegisterContainer(CachingConnectionFactory connectionFactory, BusConfig config) {
        this(connectionFactory, config, MsgType.TIMELY);
    }

    protected AbstractRabbitRegisterContainer(CachingConnectionFactory connectionFactory, BusConfig config, MsgType msgType) {
        this.connectionFactory = connectionFactory;
        this.config = config;
        this.msgType = msgType;
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
        try (Channel channel = getConnection().createChannel(false)) {
            // 根据监听器类型创建交换机
            createExchange(channel, msgType);
            // 获取事件监听器列表
            // 对每个监听器，根据其并发级别创建消费者
            getListeners().forEach(listener -> {
                Func.pollRun(listener.getConcurrency(), () -> {
                    // 创建消费者的过程可能会抛出IOException
                    try {
                        createListener(channel, listener);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            });
        } catch (Exception e) {
            // 如果应用尚未启动且捕获到异常，则终止应用
            if (!applicationStarted) {
                log.error("[Eventbus register error] ", e);
                System.exit(1);
            }
            // 将捕获到的异常包装为EventBusException并抛出
            throw new EventBusException(e);
        }
        // 设置应用启动状态为true
        applicationStarted = true;
    }

    private void createListener(Channel channel, Listener listener) throws IOException {
        // 设置通道的消息批量处理大小
        channel.basicQos(config.getMsgBatchSize());
        // 生成并声明队列，队列名称基于监听器动态生成
        String queueName = generateQueueName(listener);
        channel.queueDeclare(queueName, true, false, false, null);
        for (String code : listener.getCodes()) {
            String routingKey = generateRoutingKey(msgType, listener, code);
            channel.queueBind(queueName, exchangeName, routingKey);
        }
        if (msgType != listener.getType()) {
            return;
        }
        createConsumer(listener, queueName);
    }

    private void createConsumer(Listener listener, String queueName) throws IOException {
        Channel channel = createChannel();
        // 消费者开始监听队列，处理消息
        channel.basicConsume(queueName, false, new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                // 临时修改线程名称，以便于问题追踪
                String oldName = Func.reThreadName(BusConstant.THREAD_NAME);
                try {
                    // 处理接收到的消息，并确认消息处理成功
                    deliver(listener, body);
                    channel.basicAck(envelope.getDeliveryTag(), false);
                } catch (Exception e) {
                    // 记录消息处理失败的日志，并重试或拒绝消息
                    log.error("[Eventbus error] ", e);
                    channel.basicNack(envelope.getDeliveryTag(), false, true);
                } finally {
                    // 恢复线程名称
                    Thread.currentThread().setName(oldName);
                }
            }
        });
    }

    private String generateRoutingKey(MsgType msgType, Listener listener, String code) {
        String topic = Func.getTopic(listener.getServiceId(), code);
        if (msgType.isDelay()) {
            if (listener.getType().isDelay()) {
                return String.format(RabbitConstant.DELAY_ROUTING_KEY, topic);
            }
            return String.format(RabbitConstant.DELAY_ROUTING_KEY, topic);
        }
        return String.format(RabbitConstant.ROUTING_KEY, topic);
    }

    private String generateQueueName(Listener listener) {
        return String.format((listener.getType().isDelay() ? RabbitConstant.DELAY_QUEUE : RabbitConstant.QUEUE), listener.getServiceId(), listener.getDeliverId());
    }

    /**
     * 创建交换机。
     * 根据消息类型（MsgType）来决定创建的交换机的名称和类型。如果消息类型为及时消息，则创建普通交换机；如果为延迟消息，则创建带有延迟特性的交换机。
     *
     * @param msgType 消息类型，用来决定交换机的名称和类型。包含是否为延迟消息和是否为及时消息的标志。
     * @throws IOException 通道创建失败时抛出的异常。
     */
    private void createExchange(Channel channel, MsgType msgType) throws IOException {
        // 如果交换机名称已不为空，则不再创建，直接返回
        if (null != this.exchangeName) {
            return;
        }
        // 根据消息类型决定交换机的具体名称和类型
        String exName = String.format(msgType.isTimely() ? RabbitConstant.EXCHANGE : RabbitConstant.DELAY_EXCHANGE, config.getServiceId());
        String type = msgType.isTimely() ? BuiltinExchangeType.TOPIC.getType() : "x-delayed-message";
        Map<String, Object> args = new HashMap<>(4);
        // 如果是延迟消息，设置延迟交换机的类型
        if (msgType.isDelay()) {
            args.put("x-delayed-type", "direct");
        }
        // 声明交换机，指定交换机名称、类型、是否持久化、是否自动删除及其他参数
        channel.exchangeDeclare(exName, type, true, false, args);
        this.exchangeName = exName;
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
