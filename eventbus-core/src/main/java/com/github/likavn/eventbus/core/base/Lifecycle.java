package com.github.likavn.eventbus.core.base;

/**
 * 各中间件实现的监听容器初始化接口
 *
 * @author likavn
 * @date 2023/5/19
 **/
public interface Lifecycle {

    /**
     * 注册,网络连接成功时调用
     *
     * @throws Exception exception
     */
    void register() throws Exception;

    /**
     * 销毁,网络断开、容器销毁时调用
     *
     * @throws Exception exception
     */
    void destroy() throws Exception;
}
