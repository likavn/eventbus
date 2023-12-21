package com.github.likavn.eventbus.core.api.interceptor;

import com.github.likavn.eventbus.core.metadata.data.Message;

/**
 * 投递成功拦截器
 *
 * @author likavn
 * @date 2023/12/17
 **/
public interface DeliverSuccessInterceptor {

    /**
     * 拦截器执行
     *
     * @param message message
     */
    void execute(Message<?> message);
}
