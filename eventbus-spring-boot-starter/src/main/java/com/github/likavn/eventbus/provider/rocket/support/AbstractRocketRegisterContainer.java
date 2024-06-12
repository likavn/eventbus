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
import com.github.likavn.eventbus.core.metadata.BusConfig;
import com.github.likavn.eventbus.core.metadata.MsgType;
import com.github.likavn.eventbus.core.metadata.support.Listener;
import com.github.likavn.eventbus.core.utils.Func;
import com.github.likavn.eventbus.provider.AcquireListeners;
import com.github.likavn.eventbus.provider.rocket.constant.RocketConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.apache.rocketmq.spring.autoconfigure.RocketMQProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * AbstractListenerContainer
 *
 * @author likavn
 * @date 2024/1/20
 * @since 2.2
 **/
@Slf4j
public abstract class AbstractRocketRegisterContainer implements AcquireListeners, Lifecycle {
    private final List<DefaultMQPushConsumer> consumers = new ArrayList<>();
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
        listeners.forEach(this::registerPushConsumer);
        starts(consumers);
    }

    /**
     * 启动多个RocketMQ推送消费者实例。
     * <p>
     * 此方法遍历给定的消费者列表，并尝试启动每个消费者。如果启动过程中遇到任何异常，
     * 则记录错误并退出应用程序。这种方法确保了如果一个消费者启动失败，整个应用程序将会停止，
     * 避免了部分消费者运行而其他消费者未运行的状态，从而维护了系统的一致性和稳定性。
     *
     * @param consumers 消费者列表，每个元素是一个DefaultMQPushConsumer实例。
     */
    private void starts(List<DefaultMQPushConsumer> consumers) {
        // 遍历消费者列表并尝试启动每个消费者
        consumers.forEach(consumer -> {
            try {
                consumer.start();
            } catch (MQClientException | IllegalStateException e) {
                // 记录启动异常并退出应用程序
                log.error("AbstractRocketRegisterContainer.starts", e);
                System.exit(1);
            }
        });
    }

    /**
     * 注册消费者
     *
     * @param listener listener
     */
    private void registerPushConsumer(Listener listener) {
        String group = null != listener.getTrigger() ? listener.getTrigger().getDeliverId() : listener.getServiceId();
        group = group.replace(".", "_").replace("#", "|");
        //1.创建消费者Consumer，制定消费者组名
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(group);
        try {
            String hostName = Func.getHostName() + "@" + Func.getPid();
            consumer.setConsumeThreadMin(listener.getConcurrency());
            consumer.setConsumeThreadMax(listener.getConcurrency());
            consumer.setInstanceName(hostName);
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
            consumers.add(consumer);
        } catch (MQClientException | IllegalStateException e) {
            log.error("AbstractRocketRegisterContainer.registerPushConsumer", e);
            System.exit(1);
        }
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
