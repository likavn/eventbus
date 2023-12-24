package com.github.likavn.eventbus.core.metadata;

/**
 * 消息类型
 *
 * @author likavn
 * @date 2023/12/21
 **/
public enum MsgType {
    /**
     * 及时消息
     */
    TIMELY {
        @Override
        public boolean isTimely() {
            return true;
        }
    },

    /**
     * 延迟消息
     */
    DELAY {
        @Override
        public boolean isDelay() {
            return true;
        }
    };

    public boolean isTimely() {
        return false;
    }

    public boolean isDelay() {
        return false;
    }
}
