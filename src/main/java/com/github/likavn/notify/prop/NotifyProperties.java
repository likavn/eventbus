package com.github.likavn.notify.prop;

import lombok.Data;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 配置
 *
 * @author likavn
 * @since 2023/01/01
 */
@Data
@ToString
@ConfigurationProperties(prefix = "notify")
public class NotifyProperties {
    /**
     * 消息引擎类别（redis、rabbitmq）
     */
    private String type;

    /**
     * 延时消息，消费者数量
     */
    private Integer delayConsumerNum = 1;

    /**
     * 订阅消息，消费者数量
     */
    private Integer subConsumerNum = 1;

    /**
     * 消息投递失败时配置信息
     */
    private Fail fail = new Fail();

    /**
     * redis配置
     */
    private Redis redis = new Redis();

    /**
     * rabbitMq配置
     */
    private RabbitMq rabbitMq = new RabbitMq();

    /**
     * redis配置
     */
    @Data
    @ToString
    public static class Fail {
        /**
         * 重试次数
         */
        private Integer retryNum = 3;

        /**
         * 订阅消息-下次触发时间，单位：秒
         */
        private Long subNextTime = 3L;

        /**
         * 延时消息-下次触发时间，单位：秒
         */
        private Long delayNextTime = 3L;
    }

    /**
     * redis配置
     */
    @Data
    @ToString
    public static class Redis {
        /**
         * 延时消息，处理单条数据超时时间,单位：秒
         */
        private Long delayDeliverTimeout = 15L;
        /**
         * 订阅消息，获取单个分组下未ack的{subBatchSize}条消息的处理超时时间,单位：秒
         */
        private Long subUnAckGroupLockTimeout = 30L;
        /**
         * 订阅消息线程池大小
         */
        private Integer subExecutorPoolSize = 10;
        /**
         * 订阅消息投递超时时间,单位：秒
         */
        private Long subDeliverTimeout = 10L;
        /**
         * 订阅消息一次性最多拉取多少条消息
         */
        private Integer subBatchSize = 10;
    }

    /**
     * redis配置
     */
    @Data
    @ToString
    public static class RabbitMq {
    }
}
