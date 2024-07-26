package com.github.likavn.eventbus.demo.listener;

import com.github.likavn.eventbus.core.annotation.Fail;
import com.github.likavn.eventbus.core.annotation.Polling;
import com.github.likavn.eventbus.core.api.MsgListener;
import com.github.likavn.eventbus.core.metadata.data.Message;
import com.github.likavn.eventbus.demo.constant.MsgConstant;
import com.github.likavn.eventbus.demo.domain.TMsg;
import com.github.likavn.eventbus.demo.domain.TestBody;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author likavn
 * @date 2024/01/01
 **/
@Slf4j
@Service
public class DemoMsgListener extends MsgListener<TestBody> {
    protected DemoMsgListener() {
        super(
                // 订阅的消息编码
                MsgConstant.DEMO_MSG_LISTENER,
                // 并发数
                2);
    }

    @Override
    @Polling(count = 2, interval = "$count * $intervalTime + 5")
    @Fail(callMethod = "exceptionHandler", retryCount = 1, nextTime = 5)
    public void onMessage(Message<TestBody> message) {
        TestBody body = message.getBody();
        log.info("接收数据: {}", message.getRequestId());
        //   throw new RuntimeException("DemoMsgListener test");

        if (message.getDeliverCount() > 1) {
            // 终止轮询
            Polling.Keep.over();
        }
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
