package com.github.likavn.notify.config;

import com.github.likavn.notify.api.SubscribeMsgListener;
import com.github.likavn.notify.domain.MetaServiceProperty;
import com.github.likavn.notify.domain.SubMsgConsumer;
import com.github.likavn.notify.prop.NotifyProperties;
import com.github.likavn.notify.provider.rabbitmq.config.NotifyRabbitMqConfig;
import com.github.likavn.notify.provider.redis.config.NotifyRedisConfig;
import com.github.likavn.notify.utils.SpringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 配置
 *
 * @author likavn
 * @since 2023/01/01
 */
@Slf4j
@Configuration
@ImportAutoConfiguration({NotifyRedisConfig.class, NotifyRabbitMqConfig.class})
@EnableConfigurationProperties(NotifyProperties.class)
public class NotifyConfig {
    @Bean
    @SuppressWarnings("all")
    public MetaServiceProperty notifyProperties(ApplicationContext applicationContext) {
        String appName = SpringUtil.getServiceId();
        MetaServiceProperty serviceProperty = new MetaServiceProperty();
        serviceProperty.setServiceId(appName);

        // 获取订阅器
        List<SubMsgConsumer> subMsgConsumers = new ArrayList<>();
        Map<String, SubscribeMsgListener> exitFilterMap = applicationContext.getBeansOfType(SubscribeMsgListener.class);
        if (!exitFilterMap.isEmpty()) {
            for (SubscribeMsgListener listener : exitFilterMap.values()) {
                subMsgConsumers.addAll(listener.getSubMsgConsumers());
            }
        }
        serviceProperty.setSubMsgConsumers(subMsgConsumers);
        return serviceProperty;
    }
}
