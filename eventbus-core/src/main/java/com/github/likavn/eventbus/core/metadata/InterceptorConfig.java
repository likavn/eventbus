package com.github.likavn.eventbus.core.metadata;

import com.github.likavn.eventbus.core.api.interceptor.DeliverExceptionInterceptor;
import com.github.likavn.eventbus.core.api.interceptor.DeliverSuccessInterceptor;
import com.github.likavn.eventbus.core.api.interceptor.SendAfterInterceptor;
import com.github.likavn.eventbus.core.api.interceptor.SendBeforeInterceptor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 拦截器配置信息
 *
 * @author likavn
 * @date 2023/12/17
 **/
@Data
@NoArgsConstructor
public class InterceptorConfig {

    private SendBeforeInterceptor sendBeforeInterceptor;

    private SendAfterInterceptor sendAfterInterceptor;

    private DeliverSuccessInterceptor deliverSuccessInterceptor;

    private DeliverExceptionInterceptor deliverExceptionInterceptor;

    public InterceptorConfig(SendBeforeInterceptor sendBeforeInterceptor,
                             SendAfterInterceptor sendAfterInterceptor,
                             DeliverSuccessInterceptor deliverSuccessInterceptor,
                             DeliverExceptionInterceptor deliverExceptionInterceptor) {
        this.sendBeforeInterceptor = sendBeforeInterceptor;
        this.sendAfterInterceptor = sendAfterInterceptor;
        this.deliverSuccessInterceptor = deliverSuccessInterceptor;
        this.deliverExceptionInterceptor = deliverExceptionInterceptor;
    }
}
