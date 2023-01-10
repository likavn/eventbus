package com.github.likavn.notify.api;

import com.github.likavn.notify.domain.MsgRequest;
import com.github.likavn.notify.utils.SpringUtil;

import java.util.Objects;
import java.util.UUID;

/**
 * 消息生产者
 *
 * @author likavn
 * @since 2023/01/01
 */
public interface MsgSender {
    /**
     * 通知发送接口
     *
     * @param code 业务消息类型
     * @param body 消息体
     */
    default void send(String code, Object body) {
        send(MsgRequest.builder().code(code).body(body).build());
    }

    /**
     * 通知发送接口
     *
     * @param msgRequest msgRequest
     */
    void send(MsgRequest<?> msgRequest);

    /**
     * 发送延时消息接口
     *
     * @param handler   延时处理器
     * @param body      延时消息实体
     * @param delayTime 延时时间，单位：秒
     */
    @SuppressWarnings("all")
    default void sendDelayMessage(Class<? extends DelayMsgListener> handler, Object body, long delayTime) {
        sendDelayMessage(MsgRequest.builder().handler(handler).body(body).build(), delayTime);
    }

    /**
     * 发送延时消息接口
     *
     * @param msgRequest msgRequest
     * @param delayTime  延时时间，单位：秒
     */
    void sendDelayMessage(MsgRequest<?> msgRequest, long delayTime);

    /**
     * 设置serviceId
     *
     * @param request request
     * @return t
     */
    @SuppressWarnings("all")
    default boolean initRequest(MsgRequest<?> request) {
        if (null == request.getBody()) {
            return false;
        }
        if (!Objects.nonNull(request.getServiceId())) {
            request.setServiceId(SpringUtil.getAppName());
        }
        if (!Objects.nonNull(request.getRequestId())) {
            request.setRequestId(UUID.randomUUID().toString().replaceAll("-", ""));
        }
        if (null == request.getHandlerNum()) {
            request.setHandlerNum(1);
        }
        request.setBodyClass(request.getBody().getClass());
        return true;
    }
}
