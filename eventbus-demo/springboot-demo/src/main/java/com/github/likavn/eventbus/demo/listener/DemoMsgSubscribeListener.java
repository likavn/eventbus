package com.github.likavn.eventbus.demo.listener;

import com.github.likavn.eventbus.core.annotation.Fail;
import com.github.likavn.eventbus.core.api.MsgSubscribeListener;
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
public class DemoMsgSubscribeListener extends MsgSubscribeListener<TMsg> {
    protected DemoMsgSubscribeListener() {
        super(MsgConstant.TEST_MSG_SUBSCRIBE_LISTENER);
    }

    @Override
    @Fail(callMethod = "exceptionHandler", retry = 2, nextTime = 5)
    public void onMessage(Message<TMsg> message) {
        TMsg body = message.getBody();
        log.info("接收数据: {}", message.getRequestId());
        //  throw new RuntimeException("DemoMsgSubscribeListener test");
    }

    /**
     * 消息投递失败处理
     *
     * @param message
     * @param throwable
     */
    public void exceptionHandler(Message<TMsg> message, Throwable throwable) {
        log.error("消息投递失败！: {}，{}", message.getRequestId(), throwable.getMessage());
    }
}
