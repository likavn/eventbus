package com.github.likavn.eventbus.prop;

import com.github.likavn.eventbus.core.metadata.BusConfig;
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
    private Integer consumerNum = 2;

    /**
     * 节点联通性配置
     */
    private BusConfig.TestConnect testConnect = new BusConfig.TestConnect();

    /**
     * 消息投递失败时配置信息
     */
    private BusConfig.Fail fail = new BusConfig.Fail();
}
