package com.github.likavn.eventbus.demo.interceptor;

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
public class DemoDeliverThrowableInterceptor implements DeliverThrowableInterceptor {
    @Resource
    private BsHelper bsHelper;

    @Override
    public void execute(Request<String> request, Throwable throwable) {
        log.info("DemoDeliverThrowableInterceptor execute 最后一次投递失败！->{}", throwable.getMessage());
        bsHelper.deliverException(request, throwable);
    }
}
