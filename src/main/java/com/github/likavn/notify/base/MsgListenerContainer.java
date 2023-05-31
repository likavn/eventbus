package com.github.likavn.notify.base;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

/**
 * 监听器初始化接口
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
     * 初始化
     */
    void register();

    /**
     * 销毁
     */
    void destroy();
}
