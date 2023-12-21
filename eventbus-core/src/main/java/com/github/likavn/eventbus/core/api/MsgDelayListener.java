package com.github.likavn.eventbus.core.api;


import com.github.likavn.eventbus.core.metadata.data.Message;

/**
 * 延时消息监听器
 *
 * @author likavn
 * @since 2023/01/01
 */
public interface MsgDelayListener<T> {

    /**
     * 处理器
     *
     * @param message 消息体
     */
    void onMessage(Message<T> message);

}
