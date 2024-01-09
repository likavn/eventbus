package com.github.likavn.eventbus.core.metadata;

import lombok.*;

/**
 * 配置
 *
 * @author likavn
 * @date 2024/01/01
 */
@Data
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BusConfig {
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
    private Integer consumerNum = 2;

    /**
     * 节点联通性配置
     */
    private TestConnect testConnect = new TestConnect();

    /**
     * 消息投递失败时配置信息
     */
    private Fail fail = new Fail();

    /**
     * 节点联通性配置
     */
    @Data
    @ToString
    public static class TestConnect {
        /**
         * 轮询检测时间间隔，单位：秒
         */
        private Long pollSecond = 35L;

        /**
         * 丢失连接最长时间大于等于次值设置监听容器为连接断开，单位：秒
         */
        private Long loseConnectMaxMilliSecond = 120L;
    }

    /**
     * 消息投递失败时配置
     */
    @Data
    @ToString
    public static class Fail {
        /**
         * 消息投递失败时，一定时间内再次进行投递的次数，默认3次
         */
        private Integer retryNum = 3;

        /**
         * 下次触发时间，单位：秒，默认10秒
         */
        private Long nextTime = 10L;
    }
}
