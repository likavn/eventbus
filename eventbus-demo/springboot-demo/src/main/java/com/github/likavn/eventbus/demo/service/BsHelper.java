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
package com.github.likavn.eventbus.demo.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.likavn.eventbus.core.SubscriberRegistry;
import com.github.likavn.eventbus.core.api.MsgSender;
import com.github.likavn.eventbus.core.exception.EventBusException;
import com.github.likavn.eventbus.core.metadata.MsgType;
import com.github.likavn.eventbus.core.metadata.data.Request;
import com.github.likavn.eventbus.core.metadata.support.Subscriber;
import com.github.likavn.eventbus.core.utils.Assert;
import com.github.likavn.eventbus.demo.entity.BsConsumer;
import com.github.likavn.eventbus.demo.entity.BsData;
import com.github.likavn.eventbus.demo.mapper.BsConsumerMapper;
import com.github.likavn.eventbus.demo.mapper.BsDataMapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author likavn
 * @date 2024/3/31
 **/
@Service
public class BsHelper {
    @Resource
    private BsConsumerMapper consumerMapper;

    @Resource
    private BsDataMapper dataMapper;

    @Lazy
    @Resource
    private MsgSender msgSender;

    @Resource
    private SubscriberRegistry registry;

    /**
     * 发送消息
     *
     * @param request 消息
     */
    @Transactional(rollbackFor = Exception.class)
    public void sendMessage(Request<String> request) {
        LocalDateTime now = LocalDateTime.now();
        BsData data = BsData.builder().requestId(request.getRequestId())
                .serviceId(request.getServiceId())
                .code(request.getCode())
                .body(request.getBody())
                .createTime(now)
                .type(request.getType().getValue())
                .build();
        dataMapper.insert(data);

        String code = request.getCode();
        List<String> deliverIds = new ArrayList<>();
        if (request.getType().isDelay()) {
            deliverIds.add(request.getDeliverId());
        } else {
            List<Subscriber> subscribers = registry.getSubscribers();
            deliverIds = subscribers.stream().filter(subscriber
                            -> subscriber.getCode().equals(code))
                    .map(t -> t.getTrigger().getDeliverId()).collect(Collectors.toList());
        }

        for (String deliverId : deliverIds) {
            BsConsumer consumer = BsConsumer.builder()
                    .requestId(request.getRequestId())
                    // 消息接收处理器（消费者ID）
                    .deliverId(deliverId)
                    .deliverCount(request.getDeliverCount())
                    .delayTime(request.getDelayTime())
                    .type(request.getType().getValue())
                    // 消息状态,待处理
                    .status(ConsumerStatus.PROCESSING.value)
                    .build();
            consumer.setCreateTime(now);
            consumer.setUpdateTime(now);
            consumerMapper.insert(consumer);
        }
    }

    /**
     * 消费成功
     */
    @Transactional(rollbackFor = Exception.class)
    public void deliverSuccess(Request<String> request) {
        BsConsumer consumer = getBsConsumerByReqIdAndDeliveryId(request.getRequestId(), request.getDeliverId());
        if (null == consumer) {
            return;
        }
        BsConsumer up = new BsConsumer();
        LocalDateTime now = LocalDateTime.now();
        up.setId(consumer.getId());
        up.setSuccessTime(now);
        up.setStatus(ConsumerStatus.SUCCESS.value);
        up.setDeliverCount(request.getDeliverCount());
        up.setUpdateTime(now);
        consumerMapper.updateById(up);
    }

    /**
     * 消费异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void deliverException(Request<String> request, Throwable throwable) {
        BsConsumer consumer = getBsConsumerByReqIdAndDeliveryId(request.getRequestId(), request.getDeliverId());
        if (null == consumer) {
            return;
        }
        BsConsumer up = new BsConsumer();
        LocalDateTime now = LocalDateTime.now();
        up.setId(consumer.getId());
        up.setStatus(ConsumerStatus.EXCEPTION.value);
        up.setDeliverCount(request.getDeliverCount());
        up.setExceptionMessage(throwable.getMessage());
        up.setExceptionStackTrace(getStackTrace(throwable));
        System.out.println();
        up.setExceptionTime(now);
        up.setUpdateTime(now);
        consumerMapper.updateById(up);
    }

    private BsConsumer getBsConsumerByReqIdAndDeliveryId(String requestId, String deliverId) {
        return consumerMapper.selectOne(Wrappers.<BsConsumer>lambdaQuery()
                .select(BsConsumer::getId)
                .eq(BsConsumer::getRequestId, requestId)
                .eq(BsConsumer::getDeliverId, deliverId)
                .last("limit 1"));
    }

    /**
     * 重新发送消息
     */
    public void reSendMessage(Long id) {
        BsConsumer consumer = consumerMapper.selectById(id);
        Assert.notNull(consumer, "consumer is null");

        BsData data = dataMapper.selectById(consumer.getRequestId());
        Assert.notNull(data, "data is null");

        MsgType msgType = MsgType.of(consumer.getType());
        if (null == msgType) {
            throw new EventBusException("msgType is null");
        }

        Request<?> request = Request.builder().requestId(data.getRequestId())
                .serviceId(data.getServiceId())
                .code(data.getCode())
                .body(data.getBody())
                // 消息接收处理器（消费者ID）ID=全类名+方法名{@link Trigger#getDeliverId()}
                .deliverId(consumer.getDeliverId())
                // 消息投递次数+1
                .deliverCount(consumer.getDeliverCount() + 1)
                .type(msgType)
                .build();
        if (msgType.isTimely()) {
            msgSender.send(request);
            return;
        }
        // 延时时间设置1秒
        request.setDelayTime(1L);
        msgSender.sendDelayMessage(request);
    }

    /**
     * 消费状态
     */
    public enum ConsumerStatus {
        /**
         * 待消费
         */
        PROCESSING(0),
        /**
         * 消费成功
         */
        SUCCESS(1),
        /**
         * 消费异常
         */
        EXCEPTION(2);

        private final Integer value;

        ConsumerStatus(Integer value) {
            this.value = value;
        }

        public Integer getValue() {
            return value;
        }
    }

    public static String getStackTrace(final Throwable throwable) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw, true);
        throwable.printStackTrace(pw);
        return sw.getBuffer().toString();
    }
}
