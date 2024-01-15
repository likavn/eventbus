package com.github.likavn.eventbus.core.constant;

import com.github.likavn.eventbus.core.api.MsgDelayListener;
import com.github.likavn.eventbus.core.api.MsgSubscribeListener;
import com.github.likavn.eventbus.core.metadata.data.Message;

/**
 * eventbus常量
 *
 * @author likavn
 * @date 2024/01/01
 */
public class BusConstant {
    private BusConstant() {
    }

    /**
     * 接口订阅器接收方法名
     *
     * @see MsgSubscribeListener#onMessage(Message)
     * @see MsgDelayListener#onMessage(Message)
     */
    public static final String ON_MESSAGE = "onMessage";

    /**
     * thread name
     */
    public static final String TASK_NAME = "eventbus-task-pool-";

    /**
     * delay thread name
     */
    public static final String DELAY_MSG_THREAD_NAME = "eventbus-delayMsg-pool-";

    /**
     * subscribe thread name
     */
    public static final String SUBSCRIBE_MSG_THREAD_NAME = "eventbus-subscribeMsg-pool-";
}
