package com.github.likavn.eventbus.core.metadata;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Getter;

/**
 * 消息类型
 *
 * @author likavn
 * @date 2024/01/01
 **/
@Getter
public enum MsgType {
    /**
     * 及时消息
     */
    TIMELY(1) {
        @Override
        public boolean isTimely() {
            return true;
        }
    },

    /**
     * 延迟消息
     */
    DELAY(2) {
        @Override
        public boolean isDelay() {
            return true;
        }
    };

    /**
     * 消息类型的值
     */
    @JSONField
    private final Integer value;

    /**
     * 构造函数
     *
     * @param value 消息类型的值
     */
    MsgType(Integer value) {
        this.value = value;
    }

    /**
     * 判断消息类型是否为及时消息
     *
     * @return true代表是及时消息，false代表不是及时消息
     */
    public boolean isTimely() {
        return false;
    }

    /**
     * 判断消息类型是否为延迟消息
     *
     * @return true代表是延迟消息，false代表不是延迟消息
     */
    public boolean isDelay() {
        return false;
    }
}
