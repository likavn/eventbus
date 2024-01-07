package com.github.likavn.eventbus.demo.listener;

import com.github.likavn.eventbus.core.api.MsgSubscribeListener;
import com.github.likavn.eventbus.core.metadata.data.Message;
import com.github.likavn.eventbus.demo.constant.MsgConstant;
import com.github.likavn.eventbus.demo.domain.TMsg;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * @author likavn
 * @date 2024/01/01
 **/
@Slf4j
@Component
public class DemoMsgSubscribeListener extends MsgSubscribeListener<TMsg> {
    protected DemoMsgSubscribeListener() {
        super(Collections.singletonList(MsgConstant.TEST_MSG_SUBSCRIBE_LISTENER));
    }

    @Override
    public void onMessage(Message<TMsg> message) {
        log.info("接收数据: {}", message);
    }
}
