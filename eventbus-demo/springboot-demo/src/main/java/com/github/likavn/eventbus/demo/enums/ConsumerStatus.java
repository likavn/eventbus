package com.github.likavn.eventbus.demo.enums;

import lombok.Getter;

import java.util.Arrays;

/**
 * 消费状态
 */
@Getter
public enum ConsumerStatus {
    /**
     * 待消费
     */
    PROCESSING(0, "待消费"),
    /**
     * 消费成功
     */
    SUCCESS(1, "成功"),
    /**
     * 消费异常
     */
    EXCEPTION(2, "异常");

    private final Integer value;
    private final String name;

    ConsumerStatus(Integer value, String name) {
        this.value = value;
        this.name = name;
    }

    public static ConsumerStatus of(Integer value) {
        return Arrays.stream(values()).filter(e -> e.value.equals(value)).findFirst().orElse(null);
    }
}
