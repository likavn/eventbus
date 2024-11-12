package com.github.likavn.eventbus.demo.interceptor;

import com.github.likavn.eventbus.core.api.interceptor.DeliverAfterInterceptor;
import com.github.likavn.eventbus.core.metadata.data.Request;
import com.github.likavn.eventbus.demo.helper.BsHelper;
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
public class DemoDeliverAfterInterceptor implements DeliverAfterInterceptor {
    @Lazy
    @Resource
    private BsHelper bsHelper;

    @Override
    public void execute(Request<String> request, Throwable throwable) {
        // 成功投递
        if (null == throwable) {
            bsHelper.deliverSuccess(request);
            return;
        }
        log.info("DemoDeliverThrowableEveryInterceptor execute->{}", throwable.getMessage());
        bsHelper.deliverException(request, throwable);
    }
}
