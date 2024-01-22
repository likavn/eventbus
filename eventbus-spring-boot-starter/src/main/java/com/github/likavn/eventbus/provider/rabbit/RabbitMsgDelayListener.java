package com.github.likavn.eventbus.provider.rabbit;

import com.github.likavn.eventbus.core.DeliveryBus;
import com.github.likavn.eventbus.core.constant.BusConstant;
import com.github.likavn.eventbus.core.metadata.BusConfig;
import com.github.likavn.eventbus.core.utils.Func;
import com.github.likavn.eventbus.provider.rabbit.constant.RabbitConstant;
import com.github.likavn.eventbus.provider.rabbit.support.AbstractCachingConnectionContainer;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * 延时消息监听处理器
 *
 * @author likavn
 * @since 2023/01/01
 */
public class RabbitMsgDelayListener extends AbstractCachingConnectionContainer {
    private static final Logger logger = LoggerFactory.getLogger(RabbitMsgDelayListener.class);
    /**
     * 是否初始化rabbitmq
     */
    private boolean isCreateExchange = false;
    private final BusConfig config;
    private final DeliveryBus deliveryBus;

    public RabbitMsgDelayListener(CachingConnectionFactory connectionFactory,
                                  BusConfig config,
                                  DeliveryBus deliveryBus) {
        super(connectionFactory);
        this.config = config;
        this.deliveryBus = deliveryBus;
    }

    @Override
    public void register() throws IOException, TimeoutException {
        createExchange();
        byte count = 1;
        while (count++ <= config.getConsumerCount()) {
            createConsumer();
        }
    }

    /**
     * 创建消费者
     */
    private void createConsumer() throws IOException {
        Channel channel = createChannel();
        channel.basicConsume(String.format(RabbitConstant.QUEUE_DELAY, config.getServiceId()),
                false,
                new DefaultConsumer(channel) {
                    @Override
                    public void handleDelivery(String consumerTag,
                                               Envelope envelope,
                                               AMQP.BasicProperties properties,
                                               byte[] body) throws IOException {
                        String oldName = Func.reThreadName(BusConstant.DELAY_MSG_THREAD_NAME);
                        try {
                            deliveryBus.deliverDelay(body);
                            channel.basicAck(envelope.getDeliveryTag(), false);
                        } catch (Exception ex) {
                            logger.error("RabbitMqDelayMsgListener", ex);
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
            // 初始创建交换机
            String exchangeName = String.format(RabbitConstant.EXCHANGE_DELAY, config.getServiceId());
            Map<String, Object> args = new HashMap<>(4);
            args.put("x-delayed-type", "direct");
            //属性参数 交换机名称 交换机类型 是否持久化 是否自动删除 配置参数
            channel.exchangeDeclare(exchangeName, "x-delayed-message", true, false, args);
            // 定义队列名称
            String queueName = String.format(RabbitConstant.QUEUE_DELAY, config.getServiceId());
            channel.queueDeclare(queueName, true, false, false, null);
            channel.queueBind(queueName, exchangeName, String.format(RabbitConstant.ROUTING_KEY_DELAY, config.getServiceId()));
            isCreateExchange = true;
        }
    }
}
