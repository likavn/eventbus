package com.github.likavn.eventbus.core.api;


import com.github.likavn.eventbus.core.base.DefaultMsgDelayListener;
import com.github.likavn.eventbus.core.metadata.data.Request;

/**
 * 消息生产者
 *
 * @author likavn
 * @date 2024/01/01
 */
public interface MsgSender {
    /**
     * 通知发送接口
     * serviceId默认为本服务ID
     *
     * @param code 业务消息类型
     * @param body 消息体
     */
    default void send(String code, Object body) {
        send(Request.builder().code(code).body(body).build());
    }

    /**
     * 通知发送接口
     *
     * @param serviceId 服务ID
     * @param code      业务消息类型
     * @param body      消息体
     */
    default void send(String serviceId, String code, Object body) {
        send(Request.builder().serviceId(serviceId).code(code).body(body).build());
    }

    /**
     * 通知发送接口
     *
     * @param request request
     */
    void send(Request<?> request);

    /**
     * 发送延时消息接口
     *
     * @param listener  延时处理器
     * @param body      延时消息实体
     * @param delayTime 延时时间，单位：秒
     */
    @SuppressWarnings("all")
    default void sendDelayMessage(Class<? extends MsgDelayListener> listener, Object body, long delayTime) {
        sendDelayMessage(listener, null, body, delayTime);
    }

    /**
     * 发送延时消息接口
     *
     * @param listener  延时处理器
     * @param code      延时消息类型
     * @param body      延时消息实体
     * @param delayTime 延时时间，单位：秒
     */
    @SuppressWarnings("all")
    default void sendDelayMessage(Class<? extends MsgDelayListener> listener, String code, Object body, long delayTime) {
        sendDelayMessage(Request.builder().delayListener(listener).code(code).body(body).delayTime(delayTime).build());
    }

    /**
     * 发送延时消息接口
     *
     * @param code      延时消息类型
     * @param body      延时消息实体
     * @param delayTime 延时时间，单位：秒
     */
    @SuppressWarnings("all")
    default void sendDelayMessage(String code, Object body, long delayTime) {
        sendDelayMessage("", code, body, delayTime);
    }

    /**
     * 发送延时消息接口
     *
     * @param serviceId 服务ID
     * @param code      延时消息类型
     * @param body      延时消息实体
     * @param delayTime 延时时间，单位：秒
     */
    @SuppressWarnings("all")
    default void sendDelayMessage(String serviceId, String code, Object body, long delayTime) {
        sendDelayMessage(Request.builder().delayListener(DefaultMsgDelayListener.class).serviceId(serviceId).code(code).body(body).delayTime(delayTime).build());
    }

    /**
     * 发送延时消息接口
     *
     * @param request request
     */
    void sendDelayMessage(Request<?> request);
}
