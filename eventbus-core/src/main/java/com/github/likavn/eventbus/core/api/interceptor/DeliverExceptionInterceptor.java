package com.github.likavn.eventbus.core.api.interceptor;

import com.github.likavn.eventbus.core.metadata.data.Message;

/**
 * 投递错误拦截器
 *
 * @author likavn
 * @date 2023/12/17
 **/
public interface DeliverExceptionInterceptor {

    /**
     * 拦截器执行
     *
     * @param message   message
     * @param exception ex
     */
    void execute(Message<?> message, Exception exception);
}
