package com.github.likavn.eventbus.demo.listener;

import com.github.likavn.eventbus.core.annotation.EventbusListener;
import com.github.likavn.eventbus.core.annotation.FailRetry;
import com.github.likavn.eventbus.core.api.MsgDelayListener;
import com.github.likavn.eventbus.core.metadata.data.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author likavn
 * @date 2024/01/01
 **/
@Slf4j
@Component
@EventbusListener
public class MsgDelayListenerClassName implements MsgDelayListener<String> {
    @Override
    @FailRetry(count = 1, nextTime = 2)
    public void onMessage(Message<String> message) {
        String body = message.getBody();
        log.info("接收消息: {}", message.getRequestId());
    }
}
