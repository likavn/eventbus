package com.github.likavn.eventbus.demo.listener;

import com.github.likavn.eventbus.core.annotation.Fail;
import com.github.likavn.eventbus.core.api.MsgListener;
import com.github.likavn.eventbus.core.metadata.data.Message;
import com.github.likavn.eventbus.demo.constant.MsgConstant;
import com.github.likavn.eventbus.demo.domain.TMsg;
import com.github.likavn.eventbus.demo.domain.TestBody;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author likavn
 * @date 2024/01/01
 **/
@Slf4j
@Component
public class DemoMsgListener extends MsgListener<TestBody> {
    protected DemoMsgListener() {
        super(MsgConstant.DEMO_MSG_LISTENER);
    }

    @Override
    @Fail(callMethod = "exceptionHandler", retryCount = 1, nextTime = 5)
    public void onMessage(Message<TestBody> message) {
        TestBody body = message.getBody();
        log.info("接收数据: {}", message.getRequestId());
     //   throw new RuntimeException("DemoMsgListener test");
    }

    /**
     * 消息投递失败处理
     *
     * @param throwable
     * @param message
     */
    public void exceptionHandler(Throwable throwable, Message<TMsg> message) {
        log.error("消息投递失败！: {}，{}", message.getRequestId(), throwable.getMessage());
    }
}
