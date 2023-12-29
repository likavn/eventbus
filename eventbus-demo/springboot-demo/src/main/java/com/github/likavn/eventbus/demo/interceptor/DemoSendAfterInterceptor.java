package com.github.likavn.eventbus.demo.interceptor;

import com.alibaba.fastjson.JSONObject;
import com.github.likavn.eventbus.core.api.interceptor.SendAfterInterceptor;
import com.github.likavn.eventbus.core.metadata.data.Request;
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
    public void execute(Request<?> request) {
        log.info("DemoSendAfterInterceptor execute,{}", JSONObject.toJSONString(request));
    }
}
