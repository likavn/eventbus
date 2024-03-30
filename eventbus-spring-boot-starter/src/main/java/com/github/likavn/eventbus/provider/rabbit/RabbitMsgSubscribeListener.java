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

import com.github.likavn.eventbus.core.DeliveryBus;
import com.github.likavn.eventbus.core.constant.BusConstant;
import com.github.likavn.eventbus.core.metadata.BusConfig;
import com.github.likavn.eventbus.core.metadata.support.Subscriber;
import com.github.likavn.eventbus.core.utils.Func;
import com.github.likavn.eventbus.provider.rabbit.constant.RabbitConstant;
import com.github.likavn.eventbus.provider.rabbit.support.AbstractCachingConnectionContainer;
import com.rabbitmq.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * rabbitMq消息订阅器
 *
 * @author likavn
 * @since 2023/01/01
 **/
public class RabbitMsgSubscribeListener extends AbstractCachingConnectionContainer {
    private static final Logger logger = LoggerFactory.getLogger(RabbitMsgSubscribeListener.class);
    /**
     * 是否创建交换机
     */
    private boolean isCreateExchange = false;
    private final BusConfig config;
    private final DeliveryBus deliveryBus;
    private final List<Subscriber> subscribers;
    private final String serviceExchangeName;

    public RabbitMsgSubscribeListener(CachingConnectionFactory connectionFactory,
                                      BusConfig config,
                                      DeliveryBus deliveryBus,
                                      List<Subscriber> subscribers) {
        super(connectionFactory);
        this.config = config;
        this.deliveryBus = deliveryBus;
        this.subscribers = subscribers;
        this.serviceExchangeName = String.format(RabbitConstant.EXCHANGE, config.getServiceId());
    }

    @Override
    public void register() throws IOException, TimeoutException {
        createExchange();
        for (Subscriber subscriber : subscribers) {
            int num = 0;
            while (num++ < config.getConsumerCount()) {
                createConsumer(subscriber);
            }
        }
    }

    /**
     * 创建消费者
     *
     * @param subscriber 订阅者
     * @throws IOException IO异常
     */
    private void createConsumer(Subscriber subscriber) throws IOException {
        Channel channel = createChannel();
        // 定义队列名称
        String queueName = String.format(RabbitConstant.QUEUE, subscriber.getTopic(), subscriber.getTrigger().getDeliverId());
        channel.queueDeclare(queueName, true, false, false, null);
        // 设置路由key
        channel.queueBind(queueName, serviceExchangeName, String.format(RabbitConstant.ROUTING_KEY, subscriber.getTopic()));

        // 使用DefaultConsumer进行消息消费
        channel.basicConsume(queueName, false, new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag,
                                       Envelope envelope,
                                       AMQP.BasicProperties properties,
                                       byte[] body) throws IOException {
                String oldName = Func.reThreadName(BusConstant.SUBSCRIBE_MSG_THREAD_NAME);
                try {
                    deliveryBus.deliverTimely(subscriber, body);
                    channel.basicAck(envelope.getDeliveryTag(), false);
                } catch (Exception ex) {
                    logger.error("RabbitMsgSubscribeListener", ex);
                    channel.basicNack(envelope.getDeliveryTag(), false, true);
                } finally {
                    Thread.currentThread().setName(oldName);
                }
            }
        });
    }


    /**
     * 创建交换机
     */
    private void createExchange() throws IOException, TimeoutException {
        if (isCreateExchange) {
            return;
        }
        try (Channel channel = createChannel()) {
            channel.exchangeDeclare(serviceExchangeName,
                    BuiltinExchangeType.TOPIC, true, false, Collections.emptyMap());
            isCreateExchange = true;
        }
    }

}
