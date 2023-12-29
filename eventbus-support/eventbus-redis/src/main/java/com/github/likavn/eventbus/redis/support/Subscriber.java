package com.github.likavn.eventbus.redis.support;

import lombok.Data;

import java.util.function.Consumer;

/**
 * 订阅器
 *
 * @author likavn
 * @date 2024/01/01
 **/
@Data
public class Subscriber {
    private final String streamKey;
    private final String group;
    private final String name;
    private final Consumer<String> accept;
    private final io.lettuce.core.Consumer<String> consumer;

    public Subscriber(String streamKey, String group, String name, Consumer<String> accept) {
        this.streamKey = streamKey;
        this.group = group;
        this.name = name;
        this.accept = accept;
        this.consumer = io.lettuce.core.Consumer.from(group, name);
    }

    public Subscriber of(String streamKey, String group, String name, Consumer<String> accept) {
        return new Subscriber(streamKey, group, name, accept);
    }

    public void accept(String data) {
        accept.accept(data);
    }
}
