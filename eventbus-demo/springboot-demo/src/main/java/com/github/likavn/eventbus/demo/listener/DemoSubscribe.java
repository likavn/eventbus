package com.github.likavn.eventbus.demo.listener;

import com.github.likavn.eventbus.core.annotation.Fail;
import com.github.likavn.eventbus.core.annotation.Subscribe;
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
public class DemoSubscribe {

    @Subscribe(codes = MsgConstant.TEST_MSG_SUBSCRIBE,
            fail = @Fail(callMethod = "exceptionHandler", retry = 5, nextTime = 10))
    public void onMessage(Message<TMsg> msg) {
        log.info("onMessage: {}", msg);
    }

    public void exceptionHandler(Message<TMsg> msg, Exception e) {
        log.error("exceptionHandler: {}", msg, e);
    }
}
