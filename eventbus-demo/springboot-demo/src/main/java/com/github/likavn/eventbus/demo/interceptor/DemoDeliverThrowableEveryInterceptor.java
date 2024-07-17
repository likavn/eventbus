package com.github.likavn.eventbus.demo.interceptor;

import com.github.likavn.eventbus.core.api.interceptor.DeliverThrowableEveryInterceptor;
import com.github.likavn.eventbus.core.api.interceptor.DeliverThrowableInterceptor;
import com.github.likavn.eventbus.core.metadata.data.Request;
import com.github.likavn.eventbus.demo.service.BsHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author likavn
 * @date 2024/01/01
 **/
@Slf4j
@Component
public class DemoDeliverThrowableEveryInterceptor implements DeliverThrowableEveryInterceptor {
    @Override
    public void execute(Request<String> request, Throwable throwable) {
        log.info("DemoDeliverThrowableEveryInterceptor execute->{}", throwable.getMessage());
    }
}
