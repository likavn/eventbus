package com.github.likavn.eventbus.prop;

import com.github.likavn.eventbus.core.metadata.BusConfig;
import lombok.Data;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 配置
 *
 * @author likavn
 * @date 2024/01/01
 */
@Data
@ToString
@ConfigurationProperties(prefix = "eventbus")
public class BusProperties {
    /**
     * 服务ID
     */
    private String serviceId;

    /**
     * 消息引擎类别（redis、rabbitmq、rocketmq、pulsar）
     */
    private String type;

    /**
     * 消费者数量
     */
    private Integer consumerCount = 2;

    /**
     * 节点联通性配置
     */
    private BusConfig.TestConnect testConnect = new BusConfig.TestConnect();

    /**
     * 消息投递失败时配置信息
     */
    private BusConfig.Fail fail = new BusConfig.Fail();

    /**
     * redis配置
     */
    private RedisProperties redis = new RedisProperties();

    /**
     * redis配置
     */
    @Data
    @ToString
    public static class RedisProperties {
        /**
         * 订阅消息线程池大小
         */
        private Integer executorPoolSize = 10;
        /**
         * 消息超时时间，默认 5 分钟
         * 1. 超时的消息才会被重新投递
         * 2. 由于定时任务 1 分钟一次，消息超时后不会被立即重投，极端情况下消息5分钟过期后，再等 1 分钟才会被扫瞄到
         */
        private Long deliverTimeout = 60 * 5L;
        /**
         * 订阅消息一次性最多拉取多少条消息数据
         */
        private Integer batchSize = 100;
        /**
         * 待确认消息一次性最多拉取多少条消息数据
         */
        private Integer pendingMessagesBatchSize = 10000;
    }
}
