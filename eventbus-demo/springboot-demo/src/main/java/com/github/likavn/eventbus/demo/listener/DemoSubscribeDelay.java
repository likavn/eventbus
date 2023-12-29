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
    public void action(Message<TMsg> message) {
        log.info("onMessage: {}", message);
    }
}
