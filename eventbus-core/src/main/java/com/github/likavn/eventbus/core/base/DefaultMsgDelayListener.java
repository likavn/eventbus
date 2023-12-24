package com.github.likavn.eventbus.core.base;

import com.github.likavn.eventbus.core.SubscriberRegistry;
import com.github.likavn.eventbus.core.annotation.SubscribeDelay;
import com.github.likavn.eventbus.core.api.MsgDelayListener;
import com.github.likavn.eventbus.core.metadata.MsgType;
import com.github.likavn.eventbus.core.metadata.data.Message;
import com.github.likavn.eventbus.core.metadata.data.Request;
import com.github.likavn.eventbus.core.metadata.support.Subscriber;
import lombok.extern.slf4j.Slf4j;

/**
 * 默认延时处理器
 * 及时消息重试及{@link SubscribeDelay}注解订阅的消息
 *
 * @author likavn
 * @date 2023/12/20
 **/
@Slf4j
public class DefaultMsgDelayListener implements MsgDelayListener<Object> {

    private final SubscriberRegistry registry;

    public DefaultMsgDelayListener(SubscriberRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void onMessage(Message<Object> message) {
        Request<?> request = (Request<?>) message;
        Subscriber subscriber;
        // 延时消息
        if (MsgType.DELAY == request.getType()) {
            subscriber = registry.getSubscriberDelay(request.getCode());
        } else {
            subscriber = registry.getSubscriber(request.getDeliverId());
        }
        if (null == subscriber) {
            log.error("deliver code={} msg Missing subscriber!", message.getCode());
            return;
        }
        subscriber.getTrigger().invoke(message);
    }
}
