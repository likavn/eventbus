/**
 * Copyright 2023-2033, likavn (likavn@163.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.likavn.eventbus.provider.rocket.support;

import com.github.likavn.eventbus.core.base.Lifecycle;
import com.github.likavn.eventbus.core.constant.BusConstant;
import com.github.likavn.eventbus.core.exception.EventBusException;
import com.github.likavn.eventbus.core.metadata.BusConfig;
import com.github.likavn.eventbus.core.metadata.MsgType;
import com.github.likavn.eventbus.core.metadata.support.Listener;
import com.github.likavn.eventbus.core.utils.Func;
import com.github.likavn.eventbus.provider.AcquireListeners;
import com.github.likavn.eventbus.provider.rocket.constant.RocketConstant;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.apache.rocketmq.spring.autoconfigure.RocketMQProperties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * AbstractListenerContainer
 *
 * @author likavn
 * @date 2024/1/20
 * @since 2.2
 **/
public abstract class AbstractRocketRegisterContainer implements AcquireListeners, Lifecycle {
    private final List<DefaultMQPushConsumer> consumers = Collections.synchronizedList(new ArrayList<>());
    protected final RocketMQProperties rocketMqProperties;
    protected final BusConfig config;

    protected AbstractRocketRegisterContainer(RocketMQProperties rocketMqProperties, BusConfig config) {
        this.rocketMqProperties = rocketMqProperties;
        this.config = config;
    }

    @Override
    public void register() {
        if (!consumers.isEmpty()) {
            consumers.forEach(DefaultMQPushConsumer::resume);
            return;
        }

        List<Listener> listeners = getListeners();
        if (Func.isEmpty(listeners)) {
            return;
        }

        // register
        for (Listener listener : listeners) {
            Func.pollRun(listener.getConcurrency(), num -> {
                try {
                    registerPushConsumer(listener, num);
                } catch (MQClientException e) {
                    throw new EventBusException(e);
                }
            });
        }
    }

    /**
     * 注册消费者
     *
     * @param listener listener
     */
    private void registerPushConsumer(Listener listener, int num) throws MQClientException {
        String group = null != listener.getTrigger() ? listener.getTrigger().getDeliverId() : listener.getServiceId();
        group = group.replace(".", "_").replace("#", "|");
        //1.创建消费者Consumer，制定消费者组名
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(group);
        String hostName = Func.getHostName() + "@" + Func.getPid();
        consumer.setInstanceName(hostName + "_" + num);
        consumer.setPullBatchSize(config.getMsgBatchSize());
        consumer.setConsumeMessageBatchMaxSize(config.getMsgBatchSize());
        //2.指定Nameserver地址
        consumer.setNamesrvAddr(rocketMqProperties.getNameServer());
        MsgType msgType = listener.getType();
        //3.订阅主题Topic和Tag
        String topic = String.format(msgType.isTimely() ? RocketConstant.QUEUE : RocketConstant.DELAY_QUEUE, listener.getTopic());
        consumer.subscribe(topic, listener.getServiceId());
        //设定消费模式：负载均衡|广播模式
        consumer.setMessageModel(MessageModel.CLUSTERING);
        //4.设置回调函数，处理消息
        // MessageListenerConcurrently  并发模式 多线程消费
        //接受消息内容
        consumer.registerMessageListener((MessageListenerConcurrently) (msgList, context) -> {
            String oldName = Func.reThreadName(BusConstant.THREAD_NAME);
            try {
                for (MessageExt msg : msgList) {
                    deliver(listener, msg);
                }
            } finally {
                Thread.currentThread().setName(oldName);
            }
            //返回值CONSUME_SUCCESS成功，消息会从mq出队
            // RECONSUME_LATER (报错/null) 失败消息会重新回到队列过一会重新投递出来给当前消费者或者其他消费者消费的
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        });
        consumer.start();
        consumers.add(consumer);
    }

    /**
     * 消费消息
     *
     * @param listener 消费者
     * @param msg      消息体
     */
    protected abstract void deliver(Listener listener, MessageExt msg);

    @Override
    public void destroy() {
        consumers.forEach(DefaultMQPushConsumer::suspend);
    }
}
