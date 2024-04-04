package com.github.likavn.eventbus.demo.listener;

import com.github.likavn.eventbus.core.annotation.SubscribeDelay;
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
public class DemoSubscribeDelay {

    @SubscribeDelay(codes = MsgConstant.TEST_MSG_DELAY_SUBSCRIBE)
    // @SubscribeDelay(codes = MsgConstant.TEST_MSG_DELAY_SUBSCRIBE,fail = @Fail(callMethod = "exceptionHandler"))
    public void action(Message<TMsg> message) {
        TMsg body = message.getBody();
        log.info("接收数据: {}", message.getRequestId());
       // throw new RuntimeException("DemoSubscribeDelay test");
    }

    public void exceptionHandler(Message<TMsg> message, Throwable throwable) {
        log.error("消息投递失败！: {}，{}", message.getRequestId(), throwable.getMessage());
    }
}
