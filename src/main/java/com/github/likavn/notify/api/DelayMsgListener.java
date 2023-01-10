package com.github.likavn.notify.api;

import com.github.likavn.notify.domain.MsgRequest;

/**
 * 延时消息监听器
 *
 * @author likavn
 * @since 2023/01/01
 */
public interface DelayMsgListener<T> {

    /**
     * 处理器
     *
     * @param msg 消息体
     */
    void onMessage(MsgRequest<T> msg);

}
