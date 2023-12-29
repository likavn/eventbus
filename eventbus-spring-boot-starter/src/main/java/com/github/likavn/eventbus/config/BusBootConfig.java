package com.github.likavn.eventbus.config;

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
import com.github.likavn.eventbus.prop.BusProperties;
import com.github.likavn.eventbus.provider.rabbit.BusBootRabbitConfig;
import com.github.likavn.eventbus.provider.redis.BusBootRedisConfig;
import com.github.likavn.eventbus.provider.rocket.BusBootRocketConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * boot 启动配置
 *
 * @author likavn
 * @date 2024/01/01
 **/
@Slf4j
@Configuration
@EnableConfigurationProperties(BusProperties.class)
@ImportAutoConfiguration({BusBootRabbitConfig.class, BusBootRedisConfig.class, BusBootRocketConfig.class})
public class BusBootConfig {

    /**
     * 事件总线配置
     */
    @Bean
    public BusConfig busConfig(BusProperties properties) {
        return BusConfig.builder().serviceId(properties.getServiceId())
                .type(properties.getType())
                .consumerNum(properties.getConsumerNum())
                .testConnect(properties.getTestConnect())
                .fail(properties.getFail())
                .build();
    }

    /**
     * 事件总线拦截器配置
     */
    @Bean
    @ConditionalOnMissingBean(InterceptorConfig.class)
    public InterceptorConfig interceptorConfig(
            @Autowired(required = false) SendBeforeInterceptor sendBeforeInterceptor,
            @Autowired(required = false) SendAfterInterceptor sendAfterInterceptor,
            @Autowired(required = false) DeliverSuccessInterceptor deliverSuccessInterceptor,
            @Autowired(required = false) DeliverExceptionInterceptor deliverExceptionInterceptor) {
        return new InterceptorConfig(sendBeforeInterceptor, sendAfterInterceptor, deliverSuccessInterceptor, deliverExceptionInterceptor);
    }

    /**
     * 事件总线订阅者注册
     */
    @Bean
    @ConditionalOnMissingBean(SubscriberRegistry.class)
    public SubscriberRegistry subscriberRegistry(ApplicationContext context, BusConfig config) {
        log.info("eventbus 开始注册订阅者");
        // Component
        Map<String, Object> beanMap = context.getBeansWithAnnotation(Component.class);
        List<Object> objects = new ArrayList<>(beanMap.values());

        // Controller
        beanMap = context.getBeansWithAnnotation(Controller.class);
        objects.addAll(beanMap.values());

        // Repository
        beanMap = context.getBeansWithAnnotation(Repository.class);
        objects.addAll(beanMap.values());

        // Service
        beanMap = context.getBeansWithAnnotation(Service.class);
        objects.addAll(beanMap.values());

        SubscriberRegistry registry = new SubscriberRegistry(config);
        registry.register(objects);
        return registry;
    }

    /**
     * 事件总线
     */
    @Bean
    @ConditionalOnBean(MsgSender.class)
    @ConditionalOnMissingBean(DeliverBus.class)
    public DeliverBus deliverBus(InterceptorConfig interceptorConfig, BusConfig config, MsgSender msgSender, SubscriberRegistry registry) {
        return new DeliverBus(interceptorConfig, config, msgSender, registry);
    }

    /**
     * 连接监控
     */
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
