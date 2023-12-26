package com.github.likavn.eventbus.core.api.interceptor;

import com.github.likavn.eventbus.core.metadata.data.Request;

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
     * @param request   request
     * @param exception ex
     */
    void execute(Request<?> request, Exception exception);
}
