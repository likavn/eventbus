package com.github.likavn.eventbus.core.api.interceptor;

import com.github.likavn.eventbus.core.metadata.data.Request;

/**
 * 投递异常全局拦截器
 * 注：消息重复投递都失败时，最后一次消息投递失败时会调用该拦截器
 *
 * @author likavn
 * @date 2024/01/01
 **/
public interface DeliverThrowableInterceptor {

    /**
     * 拦截器执行
     *
     * @param request   request
     * @param throwable t
     */
    void execute(Request<String> request, Throwable throwable);
}
