package com.github.likavn.eventbus.core.base;

import com.github.likavn.eventbus.core.SubscriberRegistry;
import com.github.likavn.eventbus.core.api.MsgDelayListener;
import com.github.likavn.eventbus.core.metadata.data.Message;
import com.github.likavn.eventbus.core.metadata.support.Subscriber;
import lombok.extern.slf4j.Slf4j;

/**
 * 默认延时处理器
 *
 * @author likavn
 * @date 2023/12/20
 **/
@Slf4j
public class DefaultMsgDelayListener implements MsgDelayListener<Object> {
    @Override
    public void onMessage(Message<Object> message) {
        Subscriber subscriber = SubscriberRegistry.getSubscriber(message.getTopic());
        if (null == subscriber) {
            log.warn("deliver code={} msg Missing subscriber!", message.getCode());
            return;
        }
        subscriber.getTrigger().invoke(message);
    }
}
