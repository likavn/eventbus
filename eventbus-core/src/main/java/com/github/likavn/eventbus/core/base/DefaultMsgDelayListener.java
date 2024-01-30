package com.github.likavn.eventbus.core.base;

import com.github.likavn.eventbus.core.annotation.SubscribeDelay;
import com.github.likavn.eventbus.core.api.MsgDelayListener;
import com.github.likavn.eventbus.core.metadata.data.Message;
import lombok.extern.slf4j.Slf4j;

/**
 * 默认延时处理器
 * 及时消息重试及{@link SubscribeDelay}注解订阅的消息
 *
 * @author likavn
 * @date 2024/01/01
 **/
@Slf4j
public class DefaultMsgDelayListener implements MsgDelayListener<Object> {

    public DefaultMsgDelayListener() {
    }

    @Override
    public void onMessage(Message<Object> message) {
    }
}
