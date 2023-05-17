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
         * 延时消息，锁超时时间,单位：秒
         */
        private Long delayTimeout = 15L;
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
        /**
         * 延时消息，消费者数量
         */
        private Integer delayConsumerNum = 2;
        /**
         * 订阅消息，消费者数量
         */
        private Integer subConsumerNum = 2;
    }
}
