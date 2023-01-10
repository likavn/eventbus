package com.github.likavn.notify.provider.rabbitmq;

import com.github.likavn.notify.constant.MsgConstant;
import com.github.likavn.notify.domain.SubMsgListener;
import com.github.likavn.notify.utils.WrapUtils;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.Connection;

import java.util.List;

/**
 * @author likavn
 * @since 2023/01/01
 **/
@Slf4j
public class RabbitMqSubscribeMsgListener {

    @SuppressWarnings("all")
    public RabbitMqSubscribeMsgListener(List<SubMsgListener> subMsgListeners,
                                        CachingConnectionFactory connectionFactory) {
        Connection newConnection = connectionFactory.createConnection();
        for (SubMsgListener subMsgListener : subMsgListeners) {
            bindListener(subMsgListener, newConnection);
        }
    }

    /**
     * mq监听绑定
     *
     * @param newConnection newConnection
     * @param code          消息类型
     */
    @SuppressWarnings("all")
    private void bindListener(SubMsgListener subMsgListener, Connection newConnection) {
        try {
            Channel createChannel = newConnection.createChannel(false);

            // 定义队列名称
            String queueName = String.format(MsgConstant.QUEUE,
                    subMsgListener.getServiceId(),
                    subMsgListener.getCode(),
                    subMsgListener.getListener().getClass().getName());

            // 声明一个队列。
            // 参数一：队列名称
            // 参数二：是否持久化
            // 参数三：是否排外  如果排外则这个队列只允许有一个消费者
            // 参数四：是否自动删除队列，如果为true表示没有消息也没有消费者连接自动删除队列
            // 参数五：队列的附加属性
            // 注意：
            // 1.声明队列时，如果已经存在则放弃声明，如果不存在则会声明一个新队列；
            // 2.队列名可以任意取值，但需要与消息接收者一致。
            // 3.下面的代码可有可无，一定在发送消息前确认队列名称已经存在RabbitMQ中，否则消息会发送失败。
            createChannel.queueDeclare(queueName, true, false, false, null);

            createChannel.queueBind(queueName,
                    MsgConstant.EXCHANGE,
                    // 设置路由key
                    String.format(MsgConstant.ROUTING, subMsgListener.getServiceId(), subMsgListener.getCode()));

            DefaultConsumer defaultConsumer = new DefaultConsumer(createChannel) {
                @Override
                public void handleDelivery(String consumerTag,
                                           Envelope envelope,
                                           AMQP.BasicProperties properties,
                                           byte[] body) {
                    try {
                        subMsgListener.getListener().receiverDelivery(WrapUtils.convertByBytes(body));
                    } catch (Exception ex) {
                        log.error("BaseMsgReceiver.initRabbitMq", ex);
                    }
                }
            };
            // 接收消息。会持续坚挺，不能关闭channel和Connection
            // 参数一：队列名称
            // 参数二：消息是否自动确认，true表示自动确认接收完消息以后会自动将消息从队列移除。否则需要手动ack消息
            // 参数三：消息接收者
            createChannel.basicConsume(queueName, true, defaultConsumer);
        } catch (Exception e) {
            log.error("BaseMsgReceiver.initRabbitMq", e);
        }
    }

}
