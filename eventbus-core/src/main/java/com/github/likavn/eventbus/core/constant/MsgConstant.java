package com.github.likavn.eventbus.core.constant;

import com.github.likavn.eventbus.core.api.MsgDelayListener;
import com.github.likavn.eventbus.core.api.MsgSubscribeListener;
import com.github.likavn.eventbus.core.metadata.data.Message;

/**
 * 通知常量
 *
 * @author likavn
 * @since 2023/01/01
 */
public class MsgConstant {
    private MsgConstant() {
    }

    /**
     * 接口订阅接收方法名
     *
     * @see MsgSubscribeListener#onMessage(Message)
     * @see MsgDelayListener#onMessage(Message)
     */
    public static final String ON_MESSAGE = "onMessage";

    /**
     * delay thread name
     */
    public static final String DELAY_MSG_THREAD_NAME = "notify-delayMsg-pool-";

    /**
     * subscribe thread name
     */
    public static final String SUBSCRIBE_MSG_THREAD_NAME = "notify-subscribeMsg-pool-";

    /**
     * subscribe unAckRetry thread name
     */
    public static final String SUBSCRIBE_MSG_UN_ACK_RETRY_THREAD_NAME = "notify-subscribeMsg#unAckRetry-pool-";
}
