package com.github.likavn.eventbus.demo.interceptor;

import com.alibaba.fastjson.JSONObject;
import com.github.likavn.eventbus.core.api.interceptor.DeliverSuccessInterceptor;
import com.github.likavn.eventbus.core.metadata.data.Request;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author likavn
 * @date 2023/12/26
 **/
@Slf4j
@Component
public class DemoDeliverSuccessInterceptor implements DeliverSuccessInterceptor {
    @Override
    public void execute(Request<?> request) {
        log.info("DemoDeliverSuccessInterceptor execute,{}", JSONObject.toJSONString(request));
    }
}
