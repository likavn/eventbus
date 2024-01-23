package com.github.likavn.eventbus.demo.interceptor;

import com.alibaba.fastjson.JSONObject;
import com.github.likavn.eventbus.core.api.interceptor.SendBeforeInterceptor;
import com.github.likavn.eventbus.core.metadata.data.Request;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author likavn
 * @date 2024/01/01
 **/
@Slf4j
@Component
public class DemoSendBeforeInterceptor implements SendBeforeInterceptor {
    @Override
    public void execute(Request<String> request) {
        log.info("发送前拦截器,{}", JSONObject.toJSONString(request));
    }
}
