package com.github.likavn.eventbus.core.metadata;

import com.github.likavn.eventbus.core.api.interceptor.DeliverSuccessInterceptor;
import com.github.likavn.eventbus.core.api.interceptor.DeliverThrowableInterceptor;
import com.github.likavn.eventbus.core.api.interceptor.SendAfterInterceptor;
import com.github.likavn.eventbus.core.api.interceptor.SendBeforeInterceptor;
import com.github.likavn.eventbus.core.metadata.data.Request;
import lombok.Setter;

/**
 * 拦截器配置信息
 *
 * @author likavn
 * @date 2024/01/01
 **/
@Setter
public class InterceptorConfig {

    private final SendBeforeInterceptor sendBeforeInterceptor;

    private final SendAfterInterceptor sendAfterInterceptor;

    private final DeliverSuccessInterceptor deliverSuccessInterceptor;

    private final DeliverThrowableInterceptor deliverThrowableInterceptor;

    public InterceptorConfig(SendBeforeInterceptor sendBeforeInterceptor,
                             SendAfterInterceptor sendAfterInterceptor,
                             DeliverSuccessInterceptor deliverSuccessInterceptor,
                             DeliverThrowableInterceptor deliverThrowableInterceptor) {
        this.sendBeforeInterceptor = sendBeforeInterceptor;
        this.sendAfterInterceptor = sendAfterInterceptor;
        this.deliverSuccessInterceptor = deliverSuccessInterceptor;
        this.deliverThrowableInterceptor = deliverThrowableInterceptor;
    }

    public void sendBeforeExecute(Request<?> request) {
        // 只有第一次发送才执行拦截器
        if (sendBeforeInterceptor != null && request.getDeliverNum() <= 1) {
            sendBeforeInterceptor.execute(request);
        }
    }

    public void sendAfterExecute(Request<?> request) {
        // 只有第一次发送才执行拦截器
        if (sendAfterInterceptor != null && request.getDeliverNum() <= 1) {
            sendAfterInterceptor.execute(request);
        }
    }

    public void deliverSuccessExecute(Request<?> request) {
        if (deliverSuccessInterceptor != null) {
            deliverSuccessInterceptor.execute(request);
        }
    }

    public void deliverThrowableExecute(Request<?> request, Throwable throwable) {
        if (deliverThrowableInterceptor != null) {
            deliverThrowableInterceptor.execute(request, throwable);
        }
    }
}
