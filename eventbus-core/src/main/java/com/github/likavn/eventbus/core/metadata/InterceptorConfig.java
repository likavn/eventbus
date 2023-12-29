package com.github.likavn.eventbus.core.metadata;

import com.github.likavn.eventbus.core.api.interceptor.DeliverExceptionInterceptor;
import com.github.likavn.eventbus.core.api.interceptor.DeliverSuccessInterceptor;
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

    private final DeliverExceptionInterceptor deliverExceptionInterceptor;

    public InterceptorConfig(SendBeforeInterceptor sendBeforeInterceptor,
                             SendAfterInterceptor sendAfterInterceptor,
                             DeliverSuccessInterceptor deliverSuccessInterceptor,
                             DeliverExceptionInterceptor deliverExceptionInterceptor) {
        this.sendBeforeInterceptor = sendBeforeInterceptor;
        this.sendAfterInterceptor = sendAfterInterceptor;
        this.deliverSuccessInterceptor = deliverSuccessInterceptor;
        this.deliverExceptionInterceptor = deliverExceptionInterceptor;
    }

    public void sendBeforeExecute(Request<?> request) {
        if (sendBeforeInterceptor != null) {
            sendBeforeInterceptor.execute(request);
        }
    }

    public void sendAfterExecute(Request<?> request) {
        if (sendAfterInterceptor != null) {
            sendAfterInterceptor.execute(request);
        }
    }

    public void deliverSuccessExecute(Request<?> request) {
        if (deliverSuccessInterceptor != null) {
            deliverSuccessInterceptor.execute(request);
        }
    }

    public void deliverExceptionExecute(Request<?> request, Exception exception) {
        if (deliverExceptionInterceptor != null) {
            deliverExceptionInterceptor.execute(request, exception);
        }
    }
}
