package com.github.likavn.eventbus.test.listener;

import com.github.likavn.eventbus.core.annotation.EventbusListener;
import com.github.likavn.eventbus.core.annotation.Fail;
import com.github.likavn.eventbus.core.api.MsgDelayListener;
import com.github.likavn.eventbus.core.metadata.data.Message;
import com.github.likavn.eventbus.test.constant.MsgConstant;
import com.github.likavn.eventbus.test.domain.TMsg;
import lombok.extern.slf4j.Slf4j;

/**
 * @author likavn
 * @date 2024/01/01
 **/
@Slf4j
//@Component
@EventbusListener(codes = MsgConstant.DEMO_MSG_DELAY_LISTENER_CODE)
public class DemoMsgDelayListenerCode implements MsgDelayListener<TMsg> {
    @Override
    @Fail(callMethod = "exceptionHandler", retryCount = 1, nextTime = 15)
    public void onMessage(Message<TMsg> message) {
        TMsg body = message.getBody();
        log.info("接收消息: {}", message.getRequestId());
        //throw new RuntimeException("DemoMsgDelayListener test");
    }

    public void exceptionHandler(Message<TMsg> message, Throwable throwable) {
        TMsg body = message.getBody();
        log.error("消息投递失败！: {}，{}", message.getRequestId(), throwable.getMessage());
    }
}
