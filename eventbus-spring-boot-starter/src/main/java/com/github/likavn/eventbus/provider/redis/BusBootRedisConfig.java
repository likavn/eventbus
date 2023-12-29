package com.github.likavn.eventbus.provider.redis;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

/**
 * redis实现配置
 *
 * @author likavn
 * @date 2024/01/01
 */
@Configuration
@ConditionalOnClass(RabbitTemplate.class)
@ConditionalOnProperty(prefix = "eventbus", name = "type", havingValue = "redis")
public class BusBootRedisConfig {

}
