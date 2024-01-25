package com.github.likavn.eventbus.core.base;

/**
 * 中间件实现监听容器初始化接口
 *
 * @author likavn
 * @date 2024/01/01
 **/
public interface Lifecycle {

    /**
     * 监听组件注册
     *
     * @throws Exception e
     */
    void register() throws Exception;

    /**
     * 监听组件销毁
     *
     * @throws Exception e
     */
    void destroy() throws Exception;
}
