package com.github.likavn.eventbus.core.metadata;

import com.github.likavn.eventbus.core.api.interceptor.DeliverSuccessInterceptor;
import com.github.likavn.eventbus.core.api.interceptor.DeliverThrowableInterceptor;
import com.github.likavn.eventbus.core.api.interceptor.SendAfterInterceptor;
import com.github.likavn.eventbus.core.api.interceptor.SendBeforeInterceptor;
import com.github.likavn.eventbus.core.metadata.data.Request;
import com.github.likavn.eventbus.core.utils.Func;
import lombok.Setter;

/**
 * 拦截器配置信息
 *
 * @author likavn
 * @date 2024/01/01
 **/
@Setter
@SuppressWarnings("all")
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

    /**
     * 发送前拦截
     *
     * @param request 请求
     */
    public void sendBeforeExecute(Request<?> request) {
        // 只有第一次发送才执行拦截器
        if (sendBeforeInterceptor != null && request.getDeliverNum() <= 1) {
            convertRequest(request);
            sendBeforeInterceptor.execute((Request<String>) request);
        }
    }

    /**
     * 发送后拦截
     *
     * @param request 请求
     */
    public void sendAfterExecute(Request<?> request) {
        // 只有第一次发送才执行拦截器
        if (sendAfterInterceptor != null && request.getDeliverNum() <= 1) {
            convertRequest(request);
            sendAfterInterceptor.execute((Request<String>) request);
        }
    }

    /**
     * 接收成功拦截
     *
     * @param request 请求
     */
    public void deliverSuccessExecute(Request<?> request) {
        if (deliverSuccessInterceptor != null) {
            convertRequest(request);
            deliverSuccessInterceptor.execute((Request<String>) request);
        }
    }

    /**
     * 接收异常拦截
     *
     * @param request   请求
     * @param throwable 异常
     */
    public void deliverThrowableExecute(Request<?> request, Throwable throwable) {
        if (deliverThrowableInterceptor != null) {
            convertRequest(request);
            deliverThrowableInterceptor.execute((Request<String>) request, throwable);
        }
    }

    /**
     * 将请求体转换为json
     *
     * @param request 请求
     */
    private void convertRequest(Request request) {
        if (!(request.getBody() instanceof String)) {
            request.setBody(Func.toJson(request.getBody()));
        }
    }
}
