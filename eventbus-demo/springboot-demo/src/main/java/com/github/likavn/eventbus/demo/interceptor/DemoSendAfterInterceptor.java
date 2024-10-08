package com.github.likavn.eventbus.demo.interceptor;

import com.github.likavn.eventbus.core.api.interceptor.SendAfterInterceptor;
import com.github.likavn.eventbus.core.metadata.data.Request;
import com.github.likavn.eventbus.core.utils.Func;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author likavn
 * @date 2024/01/01
 **/
@Slf4j
@Component
public class DemoSendAfterInterceptor implements SendAfterInterceptor {
    @Override
    public void execute(Request<String> request) {
        log.debug("发送后拦截器,{}", Func.toJson(request));
    }
}
