package com.github.likavn.eventbus.demo.interceptor;

import com.alibaba.fastjson.JSONObject;
import com.github.likavn.eventbus.core.api.interceptor.DeliverSuccessInterceptor;
import com.github.likavn.eventbus.core.metadata.data.Request;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author likavn
 * @date 2024/01/01
 **/
@Slf4j
@Component
public class DemoDeliverSuccessInterceptor implements DeliverSuccessInterceptor {
    @Override
    public void execute(Request<String> request) {
        log.debug("投递消息成功,消费者->{},msg->{}", request.getDeliverId(), JSONObject.toJSONString(request));
    }
}
