package com.github.likavn.eventbus.demo.listener;

import com.github.likavn.eventbus.core.annotation.EventbusListener;
import com.github.likavn.eventbus.core.annotation.FailRetry;
import com.github.likavn.eventbus.core.api.MsgDelayListener;
import com.github.likavn.eventbus.core.metadata.data.Message;
import com.github.likavn.eventbus.demo.constant.MsgConstant;
import com.github.likavn.eventbus.demo.domain.TMsg;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author likavn
 * @date 2024/01/01
 **/
@Slf4j
@Component
@EventbusListener(codes = MsgConstant.DEMO_MSG_DELAY_LISTENER_CODE)
public class DemoMsgDelayListenerCode implements MsgDelayListener<TMsg> {
    @Override
    @FailRetry(count = 1, nextTime = 15)
    public void onMessage(Message<TMsg> message) {
        TMsg body = message.getBody();
        log.info("接收消息: {}", message.getRequestId());
        //throw new RuntimeException("DemoMsgDelayListener test");
    }

    public void failHandler(Message<TMsg> message, Throwable throwable) {
        TMsg body = message.getBody();
        log.error("消息投递失败！: {}，{}", message.getRequestId(), throwable.getMessage());
    }
}
