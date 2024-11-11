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

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import javax.annotation.Nonnull;

import java.util.HashMap;
import java.util.Map;

/**
 * 事件总线配置环境上下文初始化器
 * 该类实现了ApplicationContextInitializer接口，用于在Spring应用启动时初始化环境属性源
 *
 * @author likavn
 * @date 2024/09/06
 */
public class EventBusEnvironmentContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    /**
     * 初始化方法，用于向环境属性源添加特定条件下的属性
     *
     * @param context 可配置的应用上下文
     */
    @Override
    public void initialize(@Nonnull ConfigurableApplicationContext context) {
        // 创建条件属性对象，用于后续设置条件属性
        ConditionProperty property = new ConditionProperty("eventBusPropertySource", context);
        // 设置属性条件，如果环境里不存在则使用默认值
        property.condition("eventbus.type", "")
                .condition("eventbus.oldType", "${eventbus.type}")
                .condition("eventbus.serviceId", "${spring.application.name}");
        // 创建属性源
        MapPropertySource propertySource = property.create();
        // 将属性源添加到环境属性列表的最后
        context.getEnvironment().getPropertySources().addLast(propertySource);
    }

    /**
     * 用于设置条件属性的辅助类
     */
    static class ConditionProperty {
        private final String name;
        private final ConfigurableEnvironment environment;
        private final Map<String, Object> map = new HashMap<>();

        /**
         * 构造函数，初始化ConditionProperty对象
         *
         * @param name    属性源名称
         * @param context 可配置的应用上下文
         */
        public ConditionProperty(String name, ConfigurableApplicationContext context) {
            this.name = name;
            this.environment = context.getEnvironment();
        }

        /**
         * 设置条件属性的方法
         * 如果环境里不存在指定的属性，则添加默认值
         *
         * @param key          属性键
         * @param defaultValue 默认值
         * @return ConditionProperty对象，支持链式调用
         */
        public ConditionProperty condition(String key, String defaultValue) {
            if (!environment.containsProperty(key)) {
                addProperty(key, defaultValue);
            }
            return this;
        }

        /**
         * 向属性映射中添加属性
         *
         * @param name  属性名
         * @param value 属性值
         */
        private void addProperty(String name, Object value) {
            map.put(name, value);
        }

        /**
         * 创建并返回MapPropertySource对象
         *
         * @return MapPropertySource对象，包含设置的属性
         */
        public MapPropertySource create() {
            return new MapPropertySource(name, map);
        }
    }
}

