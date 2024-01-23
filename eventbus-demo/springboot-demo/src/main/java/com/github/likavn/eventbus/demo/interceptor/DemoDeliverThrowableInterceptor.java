package com.github.likavn.eventbus.demo.interceptor;

import com.github.likavn.eventbus.core.api.interceptor.DeliverThrowableInterceptor;
import com.github.likavn.eventbus.core.metadata.data.Request;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author likavn
 * @date 2024/01/01
 **/
@Slf4j
@Component
public class DemoDeliverThrowableInterceptor implements DeliverThrowableInterceptor {
    @Override
    public void execute(Request<String> request, Throwable throwable) {
        log.error("消息投递失败！{}", throwable.getMessage());
    }
}
