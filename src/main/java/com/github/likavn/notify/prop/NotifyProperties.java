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
    private Integer delayConsumerNum = 2;

    /**
     * 订阅消息，消费者数量
     */
    private Integer subConsumerNum = 1;

    /**
     * 节点联通性配置
     */
    private TestConnect testConnect = new TestConnect();

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
     * 节点联通性配置
     */
    @Data
    @ToString
    public static class TestConnect {
        /**
         * 轮询检测时间间隔，单位：秒
         */
        private Long pollSecond = 15L;

        /**
         * 丢失连接最长时间大于等于次值设置监听容器为连接断开，单位：秒
         */
        private Long loseConnectMaxMilliSecond = 60L;
    }

    /**
     * 消息投递失败时配置
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
         * 延时消息，处理单条数据超时时间（超时会存在重复投递）,单位：秒
         */
        private Long delayDeliverTimeout = 15L;
        /**
         * 订阅消息线程池大小
         */
        private Integer subExecutorPoolSize = 10;
        /**
         * 订阅消息投递超时时间（超时会存在重复投递）,单位：秒
         */
        private Long subDeliverTimeout = 15L;
        /**
         * 订阅消息一次性最多拉取多少条消息数据
         */
        private Integer subBatchSize = 100;
        /**
         * 订阅消息，获取单个分组下未ack的{subBatchSize}条消息的处理超时时间,单位：秒
         */
        private Long subUnAckGroupLockTimeout = 30L;
    }

    /**
     * rabbitMq配置
     */
    @Data
    @ToString
    public static class RabbitMq {
    }
}
