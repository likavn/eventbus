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
package com.github.likavn.eventbus;

import com.github.likavn.eventbus.core.constant.BusConstant;
import com.github.likavn.eventbus.core.metadata.BusType;
import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.PropertyResolver;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Map;

/**
 * 消息中间件激活配置
 *
 * @author likavn
 * @date 2024/10/08
 **/
@Order(Ordered.HIGHEST_PRECEDENCE + 40)
public class OnEventbusActiveCondition extends SpringBootCondition {
    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Map<String, Object> attributes = metadata.getAnnotationAttributes(ConditionalOnEventbusActive.class.getName());
        assert attributes != null;
        BusType activeType = (BusType) attributes.get("value");
        boolean sender = (boolean) attributes.get("sender");
        return determineOutcome(context.getEnvironment(), activeType, sender);
    }

    private ConditionOutcome determineOutcome(PropertyResolver resolver, BusType activeType, boolean sender) {
        String type = getProperty(resolver, BusConstant.TYPE_NAME);
        String oldType = getProperty(resolver, BusConstant.OLD_TYPE_NAME);
        if (!sender) {
            if (activeType.valid(type) || activeType.valid(oldType)) {
                return ConditionOutcome.match();
            }
        } else if (activeType.valid(type)) {
            return ConditionOutcome.match();
        }
        return ConditionOutcome.noMatch(ConditionMessage.of("Active type does not match: " + activeType));
    }

    private String getProperty(PropertyResolver resolver, String key) {
        return resolver.getProperty(BusConstant.CONFIG_PREFIX + "." + key);
    }
}
