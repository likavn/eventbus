package com.github.likavn.eventbus.core.base;

/**
 * 各中间件实现的监听容器初始化接口
 *
 * @author likavn
 * @date 2023/5/19
 **/
public interface MsgListenerContainer {

    /**
     * 容器第一次初始化、网络重新连接时调用
     */
    void register() throws Exception;

    /**
     * 销毁,网络断开、容器销毁时调用
     */
    void destroy() throws Exception;
}
