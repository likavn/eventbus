package com.github.likavn.eventbus.demo.interceptor;

import com.github.likavn.eventbus.core.api.interceptor.DeliverExceptionInterceptor;
import com.github.likavn.eventbus.core.metadata.data.Request;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author likavn
 * @date 2024/01/01
 **/
@Slf4j
@Component
public class DemoDeliverExceptionInterceptor implements DeliverExceptionInterceptor {
    @Override
    public void execute(Request<?> request, Exception exception) {
        log.error("deliver exception:{}", exception.getMessage());
    }
}
