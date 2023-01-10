package com.github.likavn.notify.config;

import com.github.likavn.notify.api.MsgSender;
import com.github.likavn.notify.constant.MsgConstant;
import com.github.likavn.notify.prop.NotifyProperties;
import com.github.likavn.notify.provider.rabbitmq.RabbitMqDelayMsgListener;
import com.github.likavn.notify.provider.rabbitmq.RabbitMqMsgSender;
import com.github.likavn.notify.provider.rabbitmq.RabbitMqSubscribeMsgListener;
import com.github.likavn.notify.utils.SpringUtil;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * 通知配置
 *
 * @author likavn
 * @since 2023/01/01
 */
@Configuration
@ConditionalOnClass(RabbitTemplate.class)
@ConditionalOnProperty(prefix = "notify", name = "type", havingValue = "rabbitmq")
public class NotifyRabbitMqConfig {

    private static final String DELAY_EXCHANGE_NAME;

    private static final String DELAY_QUEUE_NAME;

    private static final String DELAY_ROUTING_NAME;

    static {
        String appName = SpringUtil.getAppName();
        DELAY_EXCHANGE_NAME = String.format(MsgConstant.DELAY_EXCHANGE, appName);
        DELAY_QUEUE_NAME = String.format(MsgConstant.DELAY_QUEUE, appName);
        DELAY_ROUTING_NAME = String.format(MsgConstant.DELAY_ROUTING_KEY, appName);
    }

    /**
     * 消息通知rabbitmq实现
     */
    @Bean
    @ConditionalOnBean(RabbitTemplate.class)
    public MsgSender rabbitsMqMsgSender(RabbitTemplate rabbitTemplate) {
        return new RabbitMqMsgSender(rabbitTemplate);
    }

    /**
     * 初始化延时事件消息监听器
     */
    @Bean
    @ConditionalOnBean(CachingConnectionFactory.class)
    public RabbitMqDelayMsgListener delayMessageListener(CachingConnectionFactory connectionFactory,
                                                         @Qualifier("liDelayQueue") Queue delayQueue) {
        return new RabbitMqDelayMsgListener(connectionFactory, delayQueue);
    }

    /**
     * 定义交换机
     */
    @Bean
    public TopicExchange liTopicExchange() {
        return new TopicExchange(MsgConstant.EXCHANGE);
    }

    /**
     * 延时队列交换机
     * 注意这里的交换机类型：CustomExchange
     */
    @Bean
    public CustomExchange liDelayExchange() {
        Map<String, Object> args = new HashMap<>(4);
        args.put("x-delayed-type", "direct");
        //属性参数 交换机名称 交换机类型 是否持久化 是否自动删除 配置参数
        return new CustomExchange(NotifyRabbitMqConfig.getDelayExchangeName(), "x-delayed-message", true, false, args);
    }

    /**
     * 延时队列
     */
    @Bean
    public Queue liDelayQueue() {
        //属性参数 队列名称 是否持久化
        return new Queue(NotifyRabbitMqConfig.getDelayQueueName(), true);
    }

    /**
     * 给延时队列绑定交换机
     */
    @Bean
    public Binding liCfgDelayBinding(@Qualifier("liDelayExchange") CustomExchange delayExchange,
                                     @Qualifier("liDelayQueue") Queue delayQueue) {
        return BindingBuilder.bind(delayQueue)
                .to(delayExchange)
                .with(NotifyRabbitMqConfig.getDelayRoutingName())
                .noargs();
    }

    @Bean
    @ConditionalOnBean(CachingConnectionFactory.class)
    public RabbitMqSubscribeMsgListener rabbitMqSubscribeMsgListener(
            CachingConnectionFactory connectionFactory, NotifyProperties config) {
        return new RabbitMqSubscribeMsgListener(config.getSubMsgListeners(), connectionFactory);
    }

    public static String getDelayExchangeName() {
        return DELAY_EXCHANGE_NAME;
    }

    public static String getDelayQueueName() {
        return DELAY_QUEUE_NAME;
    }

    public static String getDelayRoutingName() {
        return DELAY_ROUTING_NAME;
    }
}
