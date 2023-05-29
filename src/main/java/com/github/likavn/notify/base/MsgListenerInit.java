package com.github.likavn.notify.base;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

/**
 * 监听器初始化接口
 *
 * @author likavn
 * @date 2023/5/19
 **/
public interface MsgListenerInit extends ApplicationRunner {
    /**
     * 程序启动后初始化
     *
     * @param args args
     */
    @Override
    default void run(ApplicationArguments args) {
        init();
    }

    /**
     * 初始化函数，所有监听器创建都需要实现该函数
     */
    void init();
}
