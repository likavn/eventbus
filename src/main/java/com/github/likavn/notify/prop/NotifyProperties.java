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
     * redis配置
     */
    private Redis redis = new Redis();

    /**
     * redis配置
     */
    @Data
    @ToString
    public static class Redis {
        /**
         * redis分布式锁，锁超时时间,单位：秒
         */
        private Long lockTimeout = 15L;

    }
}
