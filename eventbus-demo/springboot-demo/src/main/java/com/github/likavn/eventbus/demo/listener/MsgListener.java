package com.github.likavn.eventbus.demo.listener;

import com.github.likavn.eventbus.core.annotation.EventbusListener;
import com.github.likavn.eventbus.core.annotation.FailRetry;
import com.github.likavn.eventbus.core.annotation.Polling;
import com.github.likavn.eventbus.core.annotation.ToDelay;
import com.github.likavn.eventbus.core.metadata.data.Message;
import com.github.likavn.eventbus.demo.domain.TestBody;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * @author likavn
 * @date 2024/01/01
 **/
@Slf4j
@Component
@EventbusListener(serviceId = "EBServer")
public class MsgListener implements Serializable, Cloneable, com.github.likavn.eventbus.core.api.MsgListener<TestBody> {

    @Override
    @ToDelay(firstDeliver = false, delayTime = 5)
    @Polling(count = 3, interval = "$deliverCount * 2.5")
    @FailRetry(count = 3, interval = "$deliverCount * 1.5")
    public void onMessage(Message<TestBody> msg) {
        TestBody body = msg.getBody();
        log.info("接收数据,第{}次投递，轮询{}次，失败重试{}次:RequestId:{}", msg.getDeliverCount(), msg.getPollingCount(), msg.getFailRetryCount(), msg.getRequestId());

        if (msg.getDeliverCount() == 2) {
          //  throw new RuntimeException("DemoMsgListener test");
        }

        //if (msg.getPollingCount() >= 2) {
            // 终止轮询
            //Polling.Keep.setNextTime(2);
           // Polling.Keep.over();
           // log.info("终止轮询");
       // }
    }

    @Override
    public void failHandler(Message<TestBody> message, Throwable throwable) {
        log.error("消息投递失败！: {}，{}", message.getRequestId(), throwable.getMessage());
    }
}
