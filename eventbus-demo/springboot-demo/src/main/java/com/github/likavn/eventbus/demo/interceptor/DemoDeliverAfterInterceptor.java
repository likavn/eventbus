package com.github.likavn.eventbus.demo.interceptor;

import com.github.likavn.eventbus.core.annotation.Order;
import com.github.likavn.eventbus.core.api.interceptor.DeliverAfterInterceptor;
import com.github.likavn.eventbus.core.metadata.data.Request;
import com.github.likavn.eventbus.demo.helper.BsHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 消费后置拦截器
 *
 * @author likavn
 * @date 2024/01/01
 **/
@Order(1)
@Slf4j
@Component
public class DemoDeliverAfterInterceptor implements DeliverAfterInterceptor {
    @Lazy
    @Resource
    private BsHelper bsHelper;

    @Override
    public void execute(Request<String> request, Throwable throwable) {
       // log.info("DemoDeliverAfterInterceptor execute->{}", request.getRequestId());
        // 异常为空时标识成功投递，否则为投递失败
        if (null == throwable) {
            bsHelper.deliverSuccess(request);
            return;
        }
        bsHelper.deliverException(request, throwable);
    }
}
