package com.github.likavn.eventbus.test.listener;

import com.github.likavn.eventbus.core.annotation.EventbusListener;
import com.github.likavn.eventbus.core.annotation.Fail;
import com.github.likavn.eventbus.core.api.MsgDelayListener;
import com.github.likavn.eventbus.core.metadata.data.Message;
import com.github.likavn.eventbus.test.domain.TMsg;
import com.github.likavn.eventbus.test.domain.TestDelayBody;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author likavn
 * @date 2024/01/01
 **/
@Slf4j
@Component
@EventbusListener
public class DemoMsgDelayListener2 implements MsgDelayListener<TestDelayBody> {
    @Override
    @Fail(callMethod = "exceptionHandler", retryCount = 1, nextTime = 15)
    public void onMessage(Message<TestDelayBody> message) {
        TestDelayBody body = message.getBody();
        log.info("接收消息: {}", message.getRequestId());
        //throw new RuntimeException("DemoMsgDelayListener test");
    }

    public void exceptionHandler(Message<TMsg> message, Throwable throwable) {
        TMsg body = message.getBody();
        log.error("消息投递失败！: {}，{}", message.getRequestId(), throwable.getMessage());
    }
}
