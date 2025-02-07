package com.github.likavn.eventbus.demo.interceptor;

import com.github.likavn.eventbus.core.annotation.Order;
import com.github.likavn.eventbus.core.api.interceptor.DeliverBeforeInterceptor;
import com.github.likavn.eventbus.core.metadata.data.Request;
import com.github.likavn.eventbus.demo.helper.BsHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 消费前置拦截器
 * 可以设置上下文信息
 *
 * @author likavn
 * @date 2024/01/01
 **/
@Slf4j
@Order(1)
@Component
public class DemoDeliverBeforeInterceptor3 implements DeliverBeforeInterceptor {
    @Lazy
    @Resource
    private BsHelper bsHelper;

    @Override
    public void execute(Request<String> request) {
        //log.info("DemoDeliverBeforeInterceptor3 execute->{}", request.getBody());
    }
}
