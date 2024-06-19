package com.github.likavn.eventbus.demo.listener;

import com.github.likavn.eventbus.core.annotation.Fail;
import com.github.likavn.eventbus.core.annotation.Listener;
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
public class DemoAnnListener {

    @Listener(
            // 订阅的消息编码
            codes = MsgConstant.DEMO_ANN_LISTENER,
            // 并发
            concurrency = 3,
            // 异常处理
            fail = @Fail(
                    // 调用方法
                    callMethod = "exceptionHandler",
                    // 重试次数
                    retryCount = 3,
                    // 重试间隔时间
                    nextTime = 5))
    public void action(Message<TMsg> message) {
        TMsg body = message.getBody();
        log.info("接收数据: {}", message.getRequestId());
        //throw new RuntimeException("DemoAnnListener test");
    }

    public void exceptionHandler(Message<TMsg> message, Throwable throwable) {
        log.error("消息投递失败！: {}，{}", message.getRequestId(), throwable.getMessage());
    }
}
