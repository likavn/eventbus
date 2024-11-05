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

import com.github.likavn.eventbus.core.metadata.data.MsgBody;
import com.github.likavn.eventbus.core.metadata.data.Request;
import com.github.likavn.eventbus.core.utils.Func;


/**
 * 消息发送者接口
 * <p>
 * 提供了发送消息和发送延迟消息的能力
 *
 * @author likavn
 * @date 2024/01/01
 */
public interface MsgSender {

    /**
     * 使用MsgBody对象发送消息
     * 该方法通过MsgBody对象的code方法获取消息代码，然后调用带Request参数的send方法发送消息
     *
     * @param body 消息体，包含消息代码和内容
     */
    default void send(MsgBody body) {
        send(body.code(), body);
    }

    /**
     * 发送消息请求
     * <p>
     * 本方法通过提供的消息处理程序类和消息体来构建一个消息请求，并将其发送
     * 主要用于内部实现，提供类型安全和封装
     *
     * @param handlerClz 消息处理程序类，必须是MsgListener的子类
     * @param body       消息的具体内容
     */
    default void send(Class<? extends MsgListener<?>> handlerClz, Object body) {
        // 构建请求并发送
        send(Func.getMsgCode(handlerClz), body);
    }

    /**
     * 发送消息
     *
     * @param code 消息代码
     * @param body 消息的内容
     */
    default void send(String code, Object body) {
        send(Request.builder().code(code).body(body).build());
    }

    /**
     * 发送一个已经构建好的Request对象
     * 该方法是发送消息的核心方法，接收到一个已经构建好的Request对象并发送之
     *
     * @param request 已经构建好的请求对象，包含消息代码和内容
     */
    void send(Request<?> request);

    /**
     * 发送一个MsgBody对象延迟消息
     * 该方法通过MsgBody对象的code方法获取消息代码，并调用带Request参数的sendDelayMessage方法发送延迟消息
     *
     * @param body      消息体，包含消息代码和内容
     * @param delayTime 延迟时间，单位为毫秒
     */
    default void sendDelayMessage(MsgBody body, long delayTime) {
        sendDelayMessage(body.code(), body, delayTime);
    }

    /**
     * 发送延迟消息
     *
     * @param handlerClz 消息处理类类型，必须是MsgDelayListener的子类
     * @param body       消息体，发送的实际内容
     * @param delayTime  延迟时间，单位：秒
     */
    default void sendDelayMessage(Class<? extends MsgDelayListener<?>> handlerClz, Object body, long delayTime) {
        sendDelayMessage(Func.getMsgCode(handlerClz), body, delayTime);
    }

    /**
     * 使用字符串代码和任意类型的消息体延迟发送消息
     * 该方法构建一个Request对象，并调用带Request参数的sendDelayMessage方法发送延迟消息
     *
     * @param code      消息代码，用于标识消息类型
     * @param body      消息的内容，可以是任意类型
     * @param delayTime 延迟时间，单位：秒
     */
    default void sendDelayMessage(String code, Object body, long delayTime) {
        sendDelayMessage(Request.builder().code(code).body(body).delayTime(delayTime).build());
    }

    /**
     * 延迟发送一个已经构建好的Request对象
     * 该方法是延迟发送消息的核心方法，接收到一个已经构建好的Request对象并在指定时间后发送之
     *
     * @param request 已经构建好的请求对象，包含消息代码和内容
     */
    void sendDelayMessage(Request<?> request);
}
