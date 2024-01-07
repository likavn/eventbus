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
    @JSONField
    private final Integer value;

    MsgType(Integer value) {
        this.value = value;
    }

    public boolean isTimely() {
        return false;
    }

    public boolean isDelay() {
        return false;
    }
}
