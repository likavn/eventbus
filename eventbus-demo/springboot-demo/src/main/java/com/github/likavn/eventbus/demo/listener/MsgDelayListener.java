package com.github.likavn.eventbus.demo.listener;

import com.github.likavn.eventbus.core.annotation.EventbusListener;
import com.github.likavn.eventbus.core.annotation.FailRetry;
import com.github.likavn.eventbus.core.annotation.Polling;
import com.github.likavn.eventbus.core.metadata.data.Message;
import com.github.likavn.eventbus.demo.domain.TestDelayBody;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author likavn
 * @date 2024/01/01
 **/
@Slf4j
@Component
@EventbusListener
public class MsgDelayListener implements com.github.likavn.eventbus.core.api.MsgDelayListener<TestDelayBody> {
    @Override
    @Polling(count = 3, interval = "3")
    @FailRetry(count = 1, nextTime = 2)
    public void onMessage(Message<TestDelayBody> message) {
        TestDelayBody body = message.getBody();
        log.info("接收消息: {}", message.getRequestId());
        //throw new RuntimeException("DemoMsgDelayListener test");
    }

//    @Override
//    public void failHandler(Message<TestDelayBody> message, Throwable throwable) {
//        TestDelayBody body = message.getBody();
//        log.error("消息投递失败！: {}，{}", message.getRequestId(), throwable.getMessage());
//    }
}
