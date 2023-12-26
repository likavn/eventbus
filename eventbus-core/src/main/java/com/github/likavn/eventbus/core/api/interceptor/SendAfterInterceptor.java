package com.github.likavn.eventbus.core.api.interceptor;

import com.github.likavn.eventbus.core.metadata.data.Request;

/**
 * 发送后拦截器
 *
 * @author likavn
 * @date 2023/12/17
 **/
public interface SendAfterInterceptor {

    /**
     * 拦截器执行
     *
     * @param request request
     */
    void execute(Request<?> request);
}
