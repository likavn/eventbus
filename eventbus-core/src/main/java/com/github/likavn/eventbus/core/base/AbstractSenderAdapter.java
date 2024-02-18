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
package com.github.likavn.eventbus.core.base;

import com.github.likavn.eventbus.core.api.MsgSender;
import com.github.likavn.eventbus.core.metadata.BusConfig;
import com.github.likavn.eventbus.core.metadata.InterceptorConfig;
import com.github.likavn.eventbus.core.metadata.MsgType;
import com.github.likavn.eventbus.core.metadata.data.Request;
import com.github.likavn.eventbus.core.utils.Assert;
import com.github.likavn.eventbus.core.utils.Func;

import java.util.Objects;
import java.util.UUID;

/**
 * 发送消息体包装处理类
 *
 * @author likavn
 * @date 2024/01/01
 */
public abstract class AbstractSenderAdapter implements MsgSender {
    private final BusConfig config;
    private final InterceptorConfig interceptorConfig;

    protected AbstractSenderAdapter(BusConfig config, InterceptorConfig interceptorConfig) {
        this.config = config;
        this.interceptorConfig = interceptorConfig;
    }

    @Override
    public void send(Request<?> request) {
        request.setType(MsgType.TIMELY);
        checkBuild(request);
        interceptorConfig.sendBeforeExecute(request);
        toSend(request);
        interceptorConfig.sendAfterExecute(request);
    }

    /**
     * 发送消息
     *
     * @param request req
     */
    public abstract void toSend(Request<?> request);

    @Override
    public void sendDelayMessage(Request<?> request) {
        request.setType(null == request.getType() ? MsgType.DELAY : request.getType());
        checkBuild(request);
        interceptorConfig.sendBeforeExecute(request);
        toSendDelayMessage(request);
        interceptorConfig.sendAfterExecute(request);
    }

    /**
     * 发送延时消息
     *
     * @param request req
     */
    public abstract void toSendDelayMessage(Request<?> request);

    /**
     * 发送消息前置操作
     *
     * @param request req
     */
    protected void checkBuild(Request<?> request) {
        // 确保传入的对象不为空
        Objects.requireNonNull(request.getBody(), "消息体不能为空");

        // 设置服务ID为默认值，如果为空的话
        request.setServiceId(Func.isEmpty(request.getServiceId()) ? config.getServiceId() : request.getServiceId());

        // 设置请求ID为默认值，如果为空的话
        request.setRequestId(Func.isEmpty(request.getRequestId()) ? UUID.randomUUID().toString().replace("-", "") : request.getRequestId());

        // 设置递送数量为默认值，如果为空的话
        request.setDeliverNum(request.getDeliverNum() != null ? request.getDeliverNum() : 1);

        // 如果延迟监听器不为空，则进行延迟时间的校验
        if (request.getDelayListener() != null) {
            Assert.isTrue(request.getDelayTime() != null && request.getDelayTime() > 0, "delayTime is null or zero");
        }
    }
}
