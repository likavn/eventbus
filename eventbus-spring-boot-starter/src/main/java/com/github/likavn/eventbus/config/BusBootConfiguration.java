/**
 * Copyright 2023-2033, likavn (likavn@163.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.likavn.eventbus.config;

import com.github.likavn.eventbus.BootConnectionWatchdog;
import com.github.likavn.eventbus.core.ConnectionWatchdog;
import com.github.likavn.eventbus.core.DeliveryBus;
import com.github.likavn.eventbus.core.ListenerRegistry;
import com.github.likavn.eventbus.core.api.MsgSender;
import com.github.likavn.eventbus.core.api.interceptor.DeliverSuccessInterceptor;
import com.github.likavn.eventbus.core.api.interceptor.DeliverThrowableInterceptor;
import com.github.likavn.eventbus.core.api.interceptor.SendAfterInterceptor;
import com.github.likavn.eventbus.core.api.interceptor.SendBeforeInterceptor;
import com.github.likavn.eventbus.core.base.Lifecycle;
import com.github.likavn.eventbus.core.base.NodeTestConnect;
import com.github.likavn.eventbus.core.metadata.BusConfig;
import com.github.likavn.eventbus.core.metadata.InterceptorConfig;
import com.github.likavn.eventbus.prop.BusProperties;
import com.github.likavn.eventbus.provider.pulsar.BusBootPulsarConfiguration;
import com.github.likavn.eventbus.provider.rabbit.config.BusBootRabbitConfiguration;
import com.github.likavn.eventbus.provider.redis.config.BusBootRedisConfiguration;
import com.github.likavn.eventbus.provider.rocket.config.BusBootRocketConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * boot 启动配置
 *
 * @author likavn
 * @date 2024/01/01
 **/
@Slf4j
@Configuration
@EnableConfigurationProperties(BusProperties.class)
@ImportAutoConfiguration({BusBootRabbitConfiguration.class,
        BusBootRedisConfiguration.class, BusBootRocketConfiguration.class, BusBootPulsarConfiguration.class})
@ConditionalOnProperty(prefix = "eventbus", name = "enable", havingValue = "true", matchIfMissing = true)
public class BusBootConfiguration {

    /**
     * 事件总线拦截器配置
     */
    @Bean
    @ConditionalOnMissingBean(InterceptorConfig.class)
    public InterceptorConfig interceptorConfig(
            @Autowired(required = false) SendBeforeInterceptor sendBeforeInterceptor,
            @Autowired(required = false) SendAfterInterceptor sendAfterInterceptor,
            @Autowired(required = false) DeliverSuccessInterceptor deliverSuccessInterceptor,
            @Autowired(required = false) DeliverThrowableInterceptor deliverExceptionInterceptor) {
        return new InterceptorConfig(sendBeforeInterceptor, sendAfterInterceptor, deliverSuccessInterceptor, deliverExceptionInterceptor);
    }

    /**
     * 事件总线订阅者注册
     */
    @Bean
    @ConditionalOnMissingBean(ListenerRegistry.class)
    public ListenerRegistry listenerRegistry(ApplicationContext context, Environment environment, BusProperties busConfig) {
        busConfig(environment, busConfig);
        // Component
        Map<String, Object> beanMap = context.getBeansWithAnnotation(Component.class);
        ListenerRegistry registry = new ListenerRegistry(busConfig);
        registry.register(beanMap.values());
        return registry;
    }

    /**
     * 事件总线配置
     */
    public void busConfig(Environment environment, BusProperties config) {
        log.info("Eventbus Initializing... {}", config.getType());
        // 自动获取服务名
        String serviceId = config.getServiceId();
        if (!StringUtils.hasLength(serviceId)) {
            serviceId = environment.getProperty("spring.application.name");
            if (null == serviceId || serviceId.isEmpty()) {
                serviceId = System.getProperties().getProperty("sun.java.command");
            }
        }
        config.setServiceId(serviceId);
    }

    /**
     * 事件总线分发器
     */
    @Bean
    @ConditionalOnBean(MsgSender.class)
    @ConditionalOnMissingBean(DeliveryBus.class)
    public DeliveryBus deliveryBus(InterceptorConfig interceptorConfig, BusConfig busConfig, MsgSender msgSender, ListenerRegistry registry) {
        return new DeliveryBus(interceptorConfig, busConfig, msgSender, registry);
    }

    /**
     * 连接监控
     */
    @Bean
    @ConditionalOnBean(NodeTestConnect.class)
    public ConnectionWatchdog connectionWatchdog(NodeTestConnect nodeTestConnect, BusConfig busConfig, List<Lifecycle> listeners) {
        return new BootConnectionWatchdog(nodeTestConnect, busConfig.getTestConnect(), listeners);
    }
}
