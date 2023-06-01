package com.github.likavn.notify.base;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

/**
 * 各中间件实现的监听容器初始化接口
 *
 * @author likavn
 * @date 2023/5/19
 **/
public interface MsgListenerContainer extends ApplicationRunner {
    /**
     * 程序启动后初始化
     *
     * @param args args
     */
    @Override
    default void run(ApplicationArguments args) {
        register();
    }

    /**
     * 容器第一次初始化、网络重新连接时调用
     */
    void register();

    /**
     * 销毁,网络断开、容器销毁时调用
     */
    void destroy();
}
