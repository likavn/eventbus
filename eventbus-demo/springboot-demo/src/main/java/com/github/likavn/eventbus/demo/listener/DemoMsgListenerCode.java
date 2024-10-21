package com.github.likavn.eventbus.demo.listener;

import com.github.likavn.eventbus.core.annotation.EventbusListener;
import com.github.likavn.eventbus.core.annotation.FailRetry;
import com.github.likavn.eventbus.core.api.MsgListener;
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
@EventbusListener(
        codes = {
                MsgConstant.DEMO_MSG_LISTENER_CODE,
                MsgConstant.DEMO_MSG_LISTENER_CODE_V2
        },
        concurrency = 2)
public class DemoMsgListenerCode implements MsgListener<TMsg> {

    @Override
    @FailRetry(count = 1, nextTime = 7)
    public void onMessage(Message<TMsg> message) {
        TMsg body = message.getBody();
        log.info("接收数据: {}", message.getRequestId());
        //throw new RuntimeException("DemoMsgListener3 test");
    }

    @Override
    public void failHandler(Message<TMsg> message, Throwable throwable) {
        log.error("消息投递失败！: {}，{}", message.getRequestId(), throwable.getMessage());
    }
}
