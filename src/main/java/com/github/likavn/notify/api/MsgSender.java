package com.github.likavn.notify.api;

/**
 * 消息生产者
 *
 * @author likavn
 * @since 2023/01/01
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
        send(null, code, body);
    }

    /**
     * 通知发送接口
     *
     * @param serviceId 服务ID
     * @param code      业务消息类型
     * @param body      消息体
     */
    void send(String serviceId, String code, Object body);

    /**
     * 发送延时消息接口
     *
     * @param handler   延时处理器
     * @param body      延时消息实体
     * @param delayTime 延时时间，单位：秒
     */
    @SuppressWarnings("all")
    default void sendDelayMessage(Class<? extends DelayMsgListener> handler, Object body, long delayTime) {
        sendDelayMessage(handler, null, body, null, delayTime);
    }

    /**
     * 发送延时消息接口
     *
     * @param handler       延时处理器
     * @param body          延时消息实体
     * @param deliverNumber 处理次数
     * @param delayTime     延时时间，单位：秒
     */
    @SuppressWarnings("all")
    default void sendDelayMessage(Class<? extends DelayMsgListener> handler, Object body, Integer deliverNumber, long delayTime) {
        sendDelayMessage(handler, null, body, deliverNumber, delayTime);
    }

    /**
     * 发送延时消息接口
     *
     * @param handler       延时处理器
     * @param code          延时消息类型
     * @param body          延时消息实体
     * @param deliverNumber 处理次数
     * @param delayTime     延时时间，单位：秒
     */
    @SuppressWarnings("all")
    void sendDelayMessage(Class<? extends DelayMsgListener> handler, String code, Object body, Integer deliverNumber, long delayTime);
}
