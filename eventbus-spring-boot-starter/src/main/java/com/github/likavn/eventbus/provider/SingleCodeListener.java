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
package com.github.likavn.eventbus.provider;

import com.github.likavn.eventbus.core.exception.EventBusException;
import com.github.likavn.eventbus.core.metadata.MsgType;
import com.github.likavn.eventbus.core.metadata.support.Listener;
import com.github.likavn.eventbus.core.utils.Assert;
import com.github.likavn.eventbus.core.utils.Func;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 单消息code监听器
 *
 * @author likavn
 * @date 2024/01/01
 */
@Slf4j
@Setter
@Getter
@EqualsAndHashCode(callSuper = false)
public class SingleCodeListener extends Listener {
    /**
     * 消息编码
     */
    private String code;

    /**
     * 是否为重试监听器
     */
    private boolean isRetry = false;

    protected SingleCodeListener(Listener listener, String code) {
        super(listener.getServiceId(), listener.getCodes(),
                listener.getConcurrency(), listener.getRetryConcurrency(),
                listener.getTrigger(), listener.getFailTrigger(), listener.getPolling());
        setType(listener.getType());
        setToDelay(listener.getToDelay());
        this.code = code;
    }

    public static SingleCodeListener of(Listener listener, String code) {
        return new SingleCodeListener(listener, code);
    }

    /**
     * 获取指定消息类型的所有监听器列表
     */
    public static <T> List<T> getMsgTypeAllListeners(
            List<Listener> listeners, FieldExpressionFunction expressionFunction, Class<? extends SingleCodeListener> clz) {
        List<SingleCodeListener> scListeners = getSingleCodeListeners(listeners);
        List<T> retList = new ArrayList<>(getMsgTypeListeners(scListeners, false, expressionFunction, clz));
        retList.addAll(getMsgTypeListeners(scListeners, true, expressionFunction, clz));
        return retList;
    }

    /**
     * 获取指定消息类型的监听器列表
     */
    @SuppressWarnings("all")
    public static <T> List<T> getMsgTypeListeners(
            List<SingleCodeListener> listeners, boolean retry, FieldExpressionFunction expressionFunction, Class<? extends SingleCodeListener> clz) {
        if (Func.isEmpty(listeners)) {
            return Collections.emptyList();
        }
        MsgType type = listeners.get(0).getType();
        List<FieldExpression> expressions = expressionFunction.get(type, retry);
        List<T> retList = new ArrayList<>(4);
        for (SingleCodeListener scListener : listeners) {
            try {
                Constructor<? extends SingleCodeListener> constructor = clz.getConstructor(SingleCodeListener.class);
                Assert.notNull(constructor, clz.getName() + " can not find constructor");
                constructor.setAccessible(true);
                SingleCodeListener newListener = constructor.newInstance(scListener);
                newListener.setRetry(retry);
                for (FieldExpression expression : expressions) {
                    String value = String.format(expression.getExpr(), expression.paramValues(scListener));
                    Func.setBean(newListener, expression.getFieldName(), value);
                }
                retList.add((T) newListener);
            } catch (Exception e) {
                throw new EventBusException(e);
            }
        }
        return retList;
    }

    /**
     * 获取所有单消息code监听器
     */
    public static List<SingleCodeListener> getSingleCodeListeners(List<Listener> listeners) {
        return listeners.stream().flatMap(t -> t.getCodes().stream().map(code -> of(t, code))).collect(Collectors.toList());
    }

    /**
     * 字段表达式函数，用于获取字段表达式列表
     */
    @FunctionalInterface
    public interface FieldExpressionFunction {
        List<FieldExpression> get(MsgType busType, boolean retry);
    }

    /**
     * 字段表达式，用于设置bean字段值
     */
    @Data
    public static class FieldExpression {
        private SFieldFunction<?, ?> field;
        private String expr;
        private Param[] params;

        private FieldExpression(SFieldFunction<?, ?> field, String expr, Param... params) {
            this.field = field;
            this.expr = expr;
            this.params = params;
        }

        public static <T, R> FieldExpression create(SFieldFunction<T, R> field, String expr, Param... params) {
            return new FieldExpression(field, expr, params);
        }

        public String getFieldName() {
            return field.getFieldName();
        }

        public String[] paramValues(SingleCodeListener listener) {
            if (Func.isEmpty(params)) {
                return new String[0];
            }
            return Stream.of(params).map(p -> p.getValue(listener)).toArray(String[]::new);
        }

        public static Builders builders() {
            return new Builders();
        }

        public static class Builders {
            private final List<FieldExpression> list = new ArrayList<>(4);

            public Builders add(FieldExpression expression) {
                list.add(create(expression.getField(), expression.getExpr(), expression.getParams()));
                return this;
            }

            public <T, R> Builders add(SFieldFunction<T, R> field, String expr, Param... params) {
                list.add(create(field, expr, params));
                return this;
            }

            public List<FieldExpression> build() {
                return list;
            }
        }

        /**
         * 参数枚举
         */
        public enum Param {
            SERVICE_ID {
                @Override
                public String getValue(SingleCodeListener listener) {
                    return listener.getServiceId();
                }
            },
            DELIVER_ID {
                @Override
                public String getValue(SingleCodeListener listener) {
                    return listener.getDeliverId();
                }
            },
            TOPIC {
                @Override
                public String getValue(SingleCodeListener listener) {
                    return Func.getTopic(listener.getServiceId(), listener.getCode());
                }
            },
            DELIVER_TOPIC {
                @Override
                public String getValue(SingleCodeListener listener) {
                    return Func.topic(listener.getServiceId(), listener.getDeliverId());
                }
            },
            FULL_TOPIC {
                @Override
                public String getValue(SingleCodeListener listener) {
                    return Func.topic(listener.getServiceId(), listener.getCode(), listener.getDeliverId());
                }
            },
            ;

            public abstract String getValue(SingleCodeListener listener);
        }
    }
}
