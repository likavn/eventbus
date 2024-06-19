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
package com.github.likavn.eventbus.core.api;

import com.github.likavn.eventbus.core.exception.EventBusException;
import com.github.likavn.eventbus.core.metadata.BusConfig;
import com.github.likavn.eventbus.core.metadata.data.Message;
import com.github.likavn.eventbus.core.metadata.data.MsgBody;
import com.github.likavn.eventbus.core.utils.Assert;
import com.github.likavn.eventbus.core.utils.Func;

import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 及时消息订阅超类
 *
 * @author likavn
 * @date 2024/01/01
 */
public abstract class MsgListener<T> {
    /**
     * 消息所属来源服务ID,服务名
     */
    private final String serviceId;

    /**
     * 消息类型，用于区分不同的消息类型
     */
    private final List<String> codes;

    /**
     * 定义并发级别，默认{@link BusConfig#getConcurrency()}。
     */
    private final Integer concurrency;

    /**
     * 构造器
     */
    @SuppressWarnings("all")
    protected MsgListener() {
        this(null, new ArrayList<>(1), null);
        Type superclass = this.getClass().getGenericSuperclass();
        Class<?> beanClz = (Class<?>) ((ParameterizedType) superclass).getActualTypeArguments()[0];
        if (Func.isInterfaceImplemented(beanClz, MsgBody.class)) {
            try {
                Constructor<?> constructor = beanClz.getConstructor();
                if (!constructor.isAccessible()) {
                    constructor.setAccessible(true);
                }
                String code = ((MsgBody) beanClz.newInstance()).code();
                codes.add(code);
            } catch (Exception e) {
                throw new EventBusException(e);
            }
        }
        Assert.notEmpty(codes, this.getClass().getName() + "msg code is not null");
    }

    /**
     * 构造器
     *
     * @param code 消息编码
     */
    protected MsgListener(String code) {
        this(Collections.singletonList(code));
    }

    /**
     * 构造器
     *
     * @param code        消息编码
     * @param concurrency 并发级别
     */
    protected MsgListener(String code, Integer concurrency) {
        this(Collections.singletonList(code), concurrency);
    }

    /**
     * 构造器
     *
     * @param codes 消息编码
     */
    protected MsgListener(List<String> codes) {
        this(null, codes);
    }

    /**
     * 构造器
     *
     * @param codes       消息编码
     * @param concurrency 并发级别
     */
    protected MsgListener(List<String> codes, Integer concurrency) {
        this(null, codes, concurrency);
    }

    /**
     * 构造器
     *
     * @param serviceId 消息服务的ID
     * @param codes     消息编码
     */
    protected MsgListener(String serviceId, List<String> codes) {
        this(serviceId, codes, null);
    }

    /**
     * 构造器
     *
     * @param serviceId   消息服务的ID
     * @param codes       消息编码
     * @param concurrency 并发级别
     */
    protected MsgListener(String serviceId, List<String> codes, Integer concurrency) {
        this.serviceId = serviceId;
        this.codes = codes;
        this.concurrency = concurrency;
    }

    public String getServiceId() {
        return serviceId;
    }

    public List<String> getCodes() {
        return codes;
    }

    public Integer getConcurrency() {
        return concurrency;
    }

    /**
     * 处理器
     *
     * @param message 消息体
     */
    public abstract void onMessage(Message<T> message);
}
