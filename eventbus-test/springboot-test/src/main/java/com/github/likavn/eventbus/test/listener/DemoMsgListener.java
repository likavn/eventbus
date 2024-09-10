package com.github.likavn.eventbus.test.listener;

import com.github.likavn.eventbus.core.annotation.EventbusListener;
import com.github.likavn.eventbus.core.annotation.Fail;
import com.github.likavn.eventbus.core.annotation.Polling;
import com.github.likavn.eventbus.core.annotation.ToDelay;
import com.github.likavn.eventbus.core.api.MsgListener;
import com.github.likavn.eventbus.core.metadata.data.Message;
import com.github.likavn.eventbus.test.domain.TMsg;
import com.github.likavn.eventbus.test.domain.TestBody;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

/**
 * @author likavn
 * @date 2024/01/01
 **/
@Slf4j
//@Component
@EventbusListener
public class DemoMsgListener implements Serializable, Cloneable, MsgListener<TestBody> {

    @Override
    @ToDelay(delayTime = 3)
    @Polling(count = 3, interval = "5")
    @Fail(callMethod = "exceptionHandler", retryCount = 3, nextTime = 5)
    public void onMessage(Message<TestBody> msg) {
        TestBody body = msg.getBody();
        log.info("接收数据,第{}次投递，轮询{}次，失败重试{}次:RequestId:{}", msg.getDeliverCount(), msg.getPollingCount(), msg.getFailRetryCount(), msg.getRequestId());
        //  throw new RuntimeException("DemoMsgListener test");
        if (msg.getDeliverCount() == 3) {
            throw new RuntimeException("DemoMsgListener test");
        }

        if (msg.getPollingCount() >= 2) {
            // 终止轮询
            Polling.Keep.over();
            log.info("终止轮询");
        }
    }

    public void exceptionHandler(Throwable throwable, Message<TMsg> message) {
        log.error("消息投递失败！: {}，{}", message.getRequestId(), throwable.getMessage());
    }

    @Override
    public DemoMsgListener clone() {
        try {
            DemoMsgListener clone = (DemoMsgListener) super.clone();
            // TODO: copy mutable state here, so the clone can't change the internals of the original
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
