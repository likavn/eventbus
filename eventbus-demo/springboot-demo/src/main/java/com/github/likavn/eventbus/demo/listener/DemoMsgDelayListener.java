package com.github.likavn.eventbus.demo.listener;

import com.github.likavn.eventbus.core.annotation.Fail;
import com.github.likavn.eventbus.core.api.MsgDelayListener;
import com.github.likavn.eventbus.core.metadata.data.Message;
import com.github.likavn.eventbus.demo.domain.TMsg;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author likavn
 * @date 2024/01/01
 **/
@Slf4j
@Component
public class DemoMsgDelayListener implements MsgDelayListener<TMsg> {
    @Override
    @Fail(callMethod = "exceptionHandler", retry = 5, nextTime = 15)
    public void onMessage(Message<TMsg> message) {
        log.info("onMessage: {}", message);
    }

    public void exceptionHandler(Message<TMsg> message, Exception e) {
        log.info("exception: {}", message);
    }
}
