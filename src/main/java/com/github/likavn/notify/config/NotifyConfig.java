package com.github.likavn.notify.config;

import com.github.likavn.notify.api.SubscribeMsgListener;
import com.github.likavn.notify.domain.SubMsgConsumer;
import com.github.likavn.notify.prop.NotifyProperties;
import com.github.likavn.notify.utils.SpringUtil;
import lombok.extern.slf4j.Slf4j;
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
public class NotifyConfig {
    @Bean
    @SuppressWarnings("all")
    public NotifyProperties notifyProperties(ApplicationContext applicationContext) {
        String appName = SpringUtil.getServiceId();
        NotifyProperties notifyProperties = new NotifyProperties();
        notifyProperties.setServiceId(appName);

        // 获取订阅器
        List<SubMsgConsumer> subMsgConsumers = new ArrayList<>();
        Map<String, SubscribeMsgListener> exitFilterMap = applicationContext.getBeansOfType(SubscribeMsgListener.class);
        if (!exitFilterMap.isEmpty()) {
            for (SubscribeMsgListener listener : exitFilterMap.values()) {
                subMsgConsumers.addAll(listener.getSubMsgConsumers());
            }
        }
        notifyProperties.setSubMsgConsumers(subMsgConsumers);
        return notifyProperties;
    }
}
