package com.github.likavn.eventbus.test.interceptor;

import com.github.likavn.eventbus.core.api.interceptor.SendBeforeInterceptor;
import com.github.likavn.eventbus.core.metadata.data.Request;
import com.github.likavn.eventbus.test.helper.BsHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author likavn
 * @date 2024/01/01
 **/
@Slf4j
@Component
public class DemoSendBeforeInterceptor implements SendBeforeInterceptor {
    @Lazy
    @Resource
    private BsHelper bsHelper;

    @Override
    public void execute(Request<String> request) {
        bsHelper.sendMessage(request);
    }
}
