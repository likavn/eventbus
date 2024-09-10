package com.github.likavn.eventbus.test.listener;

import com.github.likavn.eventbus.core.annotation.EventbusListener;
import com.github.likavn.eventbus.core.annotation.Fail;
import com.github.likavn.eventbus.core.api.MsgListener;
import com.github.likavn.eventbus.core.metadata.data.Message;
import com.github.likavn.eventbus.test.domain.TMsg;
import com.github.likavn.eventbus.test.domain.TestBody;
import lombok.extern.slf4j.Slf4j;

/**
 * @author likavn
 * @date 2024/01/01
 **/
@Slf4j
//@Component
@EventbusListener
public class DemoMsgListener2 implements MsgListener<TestBody> {

    @Override
    @Fail(callMethod = "exceptionHandler", retryCount = 3, nextTime = 7)
    public void onMessage(Message<TestBody> message) {
        TestBody body = message.getBody();
        log.info("接收数据: {}", message.getRequestId());
        // throw new RuntimeException("DemoMsgListener2 test");
    }

    public void exceptionHandler(Message<TMsg> message, Throwable throwable) {
        log.error("消息投递失败！: {}，{}", message.getRequestId(), throwable.getMessage());
    }
}
