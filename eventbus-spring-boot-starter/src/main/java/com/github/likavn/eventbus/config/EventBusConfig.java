package com.github.likavn.eventbus.config;

import com.github.likavn.eventbus.SubscriberBootRegistry;
import com.github.likavn.eventbus.core.ConnectionWatchdog;
import com.github.likavn.eventbus.core.DeliverBus;
import com.github.likavn.eventbus.core.SubscriberRegistry;
import com.github.likavn.eventbus.core.api.MsgSender;
import com.github.likavn.eventbus.core.api.interceptor.DeliverExceptionInterceptor;
import com.github.likavn.eventbus.core.api.interceptor.DeliverSuccessInterceptor;
import com.github.likavn.eventbus.core.api.interceptor.SendAfterInterceptor;
import com.github.likavn.eventbus.core.api.interceptor.SendBeforeInterceptor;
import com.github.likavn.eventbus.core.base.Lifecycle;
import com.github.likavn.eventbus.core.base.NodeTestConnect;
import com.github.likavn.eventbus.core.metadata.BusConfig;
import com.github.likavn.eventbus.core.metadata.InterceptorConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * @author likavn
 * @date 2023/12/20
 **/
@Slf4j
@Configuration
public class EventBusConfig {

    @Bean
    @ConditionalOnMissingBean(SubscriberBootRegistry.class)
    public SubscriberBootRegistry subscriptionRegistry() {
        return new SubscriberBootRegistry();
    }

    @Bean
    public BusConfig busConfig() {
        return new BusConfig();
    }

    @Bean
    @ConditionalOnMissingBean(InterceptorConfig.class)
    public InterceptorConfig interceptorConfig(
            @Autowired(required = false) SendBeforeInterceptor sendBeforeInterceptor,
            @Autowired(required = false) SendAfterInterceptor sendAfterInterceptor,
            @Autowired(required = false) DeliverSuccessInterceptor deliverSuccessInterceptor,
            @Autowired(required = false) DeliverExceptionInterceptor deliverExceptionInterceptor) {
        return new InterceptorConfig(sendBeforeInterceptor, sendAfterInterceptor, deliverSuccessInterceptor, deliverExceptionInterceptor);
    }

    @Bean
    @ConditionalOnMissingBean(SubscriberRegistry.class)
    public SubscriberRegistry subscriberRegistry(BusConfig config) {
        return new SubscriberRegistry(config);
    }

    @Bean
    @ConditionalOnMissingBean(DeliverBus.class)
    public DeliverBus deliverBus(InterceptorConfig interceptorConfig, BusConfig config, MsgSender msgSender, SubscriberRegistry registry) {
        return new DeliverBus(interceptorConfig, config, msgSender, registry);
    }

    @Bean
    @ConditionalOnBean(NodeTestConnect.class)
    public ConnectionWatchdog connectionWatchdog(ApplicationContext applicationContext, NodeTestConnect nodeTestConnect, BusConfig busConfig) {
        Collection<Lifecycle> containers = Collections.emptyList();
        Map<String, Lifecycle> containerMap = applicationContext.getBeansOfType(Lifecycle.class);
        if (!containerMap.isEmpty()) {
            containers = containerMap.values();
        }
        return new ConnectionWatchdog(nodeTestConnect, busConfig.getTestConnect(), containers);
    }
}
