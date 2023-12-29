package com.github.likavn.eventbus.core.api.interceptor;

import com.github.likavn.eventbus.core.metadata.data.Request;

/**
 * 投递成功拦截器
 *
 * @author likavn
 * @date 2024/01/01
 **/
public interface DeliverSuccessInterceptor {

    /**
     * 拦截器执行
     *
     * @param request request
     */
    void execute(Request<?> request);
}
