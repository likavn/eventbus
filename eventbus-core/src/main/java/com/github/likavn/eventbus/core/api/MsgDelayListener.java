package com.github.likavn.eventbus.core.api;

import com.github.likavn.eventbus.core.metadata.BusConfig;
import com.github.likavn.eventbus.core.metadata.data.Message;

/**
 * 延时消息监听器
 * 注：只能订阅本服务{@link BusConfig#getServiceId()}下的延时消息
 *
 * @author likavn
 * @date 2024/01/01
 */
public interface MsgDelayListener<T> {

    /**
     * 处理器
     *
     * @param message 消息体
     */
    void onMessage(Message<T> message);

}
