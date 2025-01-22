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
package com.github.likavn.eventbus.demo.helper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.likavn.eventbus.core.api.MsgSender;
import com.github.likavn.eventbus.core.exception.EventBusException;
import com.github.likavn.eventbus.core.metadata.BusConfig;
import com.github.likavn.eventbus.core.metadata.MsgType;
import com.github.likavn.eventbus.core.metadata.data.Request;
import com.github.likavn.eventbus.core.utils.Assert;
import com.github.likavn.eventbus.core.utils.Func;
import com.github.likavn.eventbus.demo.entity.BsConsumer;
import com.github.likavn.eventbus.demo.entity.BsData;
import com.github.likavn.eventbus.demo.enums.ConsumerStatus;
import com.github.likavn.eventbus.demo.mapper.BsConsumerMapper;
import com.github.likavn.eventbus.demo.mapper.BsDataMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * @author likavn
 * @date 2024/3/31
 **/
@Slf4j
@Service
public class BsHelper {
    @Lazy
    @Resource
    private BsConsumerMapper consumerMapper;
    @Lazy
    @Resource
    private BsDataMapper dataMapper;

    @Lazy
    @Resource
    private MsgSender msgSender;

    @Lazy
    @Resource
    private BusConfig config;

    /**
     * 保存消息
     *
     * @param request 消息
     */
    public void saveMessage(Request<String> request) {
        long delayTime = request.getDelayTime();
        Map<String, String> headers = request.getHeaders();
        BsData data = BsData.builder().requestId(request.getRequestId())
                .serviceId(request.getServiceId())
                .code(request.getCode())
                .type(request.getType().getValue())
                .delayTime(0 == delayTime ? null : delayTime)
                .headers(null == headers ? null : Func.toJson(headers))
                .body(request.getBody())
                .ipAddress(Func.getHostAddr())
                .createTime(LocalDateTime.now())
                .build();
        dataMapper.insert(data);
    }

    /**
     * 消费成功
     */
    public void deliverSuccess(Request<String> request) {
        BsConsumer consumer = getBsConsumerByReqIdAndDeliveryId(request);
        LocalDateTime now = LocalDateTime.now();
        consumer.setSuccessTime(now);
        consumer.setDelayTime(request.getDelayTime());
        consumer.setStatus(ConsumerStatus.SUCCESS.getValue());
        consumer.setDeliverCount(request.getDeliverCount());
        consumer.setPollingCount(request.getPollingCount());
        consumer.setFailRetryCount(request.getFailRetryCount());
        consumer.setToDelay(request.isToDelay());
        consumer.setUpdateTime(now);
        if (null != consumer.getId()) {
            consumerMapper.updateById(consumer);
            return;
        }
        consumerMapper.insert(consumer);
    }

    /**
     * 消费异常
     */
    public void deliverException(Request<String> request, Throwable throwable) {
        BsConsumer consumer = getBsConsumerByReqIdAndDeliveryId(request);
        LocalDateTime now = LocalDateTime.now();
        consumer.setStatus(ConsumerStatus.EXCEPTION.getValue());
        consumer.setDelayTime(request.getDelayTime());
        consumer.setDeliverCount(request.getDeliverCount());
        consumer.setPollingCount(request.getPollingCount());
        consumer.setFailRetryCount(request.getFailRetryCount());
        consumer.setToDelay(request.isToDelay());
        consumer.setExceptionMessage(throwable.getMessage());
        consumer.setExceptionStackTrace(getStackTrace(throwable));
        consumer.setExceptionTime(now);
        consumer.setUpdateTime(now);
        if (null != consumer.getId()) {
            consumerMapper.updateById(consumer);
            return;
        }
        consumerMapper.insert(consumer);
    }

    private BsConsumer getBsConsumerByReqIdAndDeliveryId(Request<String> request) {
        BsConsumer consumer = consumerMapper.selectOne(Wrappers.<BsConsumer>lambdaQuery()
                .select(BsConsumer::getId)
                .eq(BsConsumer::getRequestId, request.getRequestId())
                .eq(BsConsumer::getDeliverId, request.getDeliverId())
                .last("limit 1"));
        if (null != consumer) {
            return consumer;
        }
        return buildConsumer(request);
    }

    private BsConsumer buildConsumer(Request<String> request) {
        BsConsumer consumer = BsConsumer.builder()
                .requestId(request.getRequestId())
                // 这里设置的是当前监听器所在服务的ID
                .serviceId(config.getServiceId())
                .code(request.getCode())
                .type(request.getType().getValue())
                .delayTime(request.getDelayTime())
                // 消息接收处理器（消费者ID）
                .deliverId(request.getDeliverId())
                .deliverCount(request.getDeliverCount())
                .ipAddress(Func.getHostAddr())
                // 消息状态,待处理
                .status(ConsumerStatus.PROCESSING.getValue())
                .build();
        LocalDateTime now = LocalDateTime.now();
        consumer.setCreateTime(now);
        consumer.setUpdateTime(now);
        return consumer;
    }

    /**
     * 重新发送消息
     */
    public void resend(Long consumerDataId) {
        BsConsumer consumer = consumerMapper.selectById(consumerDataId);
        Assert.notNull(consumer, "consumer is null");

        BsData data = dataMapper.selectById(consumer.getRequestId());
        Assert.notNull(data, "data is null");

        // 重新发送消息
        resend(data, consumer);
    }

    /**
     * 重新发送消息
     */
    public void resend(Long requestId, String consumerDeliverId) {
        BsData data = dataMapper.selectById(requestId);
        Assert.notNull(data, "data is null");

        // 构建消息接收处理器
        BsConsumer consumer = BsConsumer.builder()
                .deliverId(consumerDeliverId)
                .deliverCount(1)
                .pollingCount(0)
                .failRetryCount(0)
                .build();

        // 重新发送消息
        resend(data, consumer);
    }

    /**
     * 重新发送消息
     */
    public void resend(BsData data, BsConsumer consumer) {
        MsgType msgType = MsgType.of(data.getType());
        if (null == msgType) {
            throw new EventBusException("msgType is null");
        }
        Request<?> request = Request.builder().requestId(data.getRequestId())
                .serviceId(data.getServiceId())
                .code(data.getCode())
                .type(msgType)
                .headers(null == data.getHeaders() ? null : Func.parseObject(data.getHeaders(), Map.class))
                .body(data.getBody())
                // 消息接收处理器（消费者ID）ID=全类名+方法名{@link Trigger#getDeliverId()}
                .deliverId(consumer.getDeliverId())
                // 消息投递次数+1
                .deliverCount(consumer.getDeliverCount() + 1)
                .build();
        // 特殊说明
        // 设置的轮询次数小于监听器配置的轮询次数，次数会触发轮询，否则不进行轮询
        request.setPollingCount(consumer.getPollingCount());

        // 失败重试次数，此处设置失败次数小于监听器配置的重试次数，则当接收消息发生异常时会出发重试，否则不进行重试
        request.setFailRetryCount(consumer.getFailRetryCount());

        // 延时时间设置1秒,所有消息重试都走延时消息
        request.setDelayTime(1L);
        request.setRetry(Boolean.TRUE);
        log.info("重发ebs消息:requestId={},deliverId={}", request.getRequestId(), request.getDeliverId());
        msgSender.sendDelayMessage(request);
    }

    public static String getStackTrace(final Throwable throwable) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw, true);
        throwable.printStackTrace(pw);
        return sw.getBuffer().toString();
    }
}
