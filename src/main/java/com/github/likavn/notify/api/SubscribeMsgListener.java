package com.github.likavn.notify.api;

import com.github.likavn.notify.base.AbstractMsgFailRetryHandler;
import com.github.likavn.notify.domain.SubMsgConsumer;
import com.github.likavn.notify.utils.SpringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 消息订阅超类
 *
 * @author likavn
 * @since 2023/01/01
 */
public abstract class SubscribeMsgListener<T> extends AbstractMsgFailRetryHandler<T> {
    /**
     * 消费者数量
     */
    private final Integer consumerNum;

    /**
     * 消息所属来源服务ID,服务名
     */
    private final String serviceId;

    /**
     * 消息类型，用于区分不同的消息类型
     */
    private final List<String> codes;

    /**
     * 构造器
     *
     * @param codes 消息编码
     */
    protected SubscribeMsgListener(List<String> codes) {
        this(null, codes);
    }

    /**
     * 构造器
     *
     * @param serviceId 消息服务的ID
     * @param codes     消息编码
     */
    protected SubscribeMsgListener(String serviceId, List<String> codes) {
        this(serviceId, codes, null, null);
    }

    /**
     * 构造器
     *
     * @param codes    消息编码
     * @param nextTime 失败时下次触发的间隔时间,单位：秒
     */
    protected SubscribeMsgListener(List<String> codes, Long nextTime) {
        this(null, codes, null, nextTime, null);
    }

    /**
     * 构造器
     *
     * @param serviceId 消息服务的ID
     * @param codes     消息编码
     * @param nextTime  失败时下次触发的间隔时间,单位：秒
     */
    protected SubscribeMsgListener(String serviceId, List<String> codes, Long nextTime) {
        this(serviceId, codes, null, nextTime, null);
    }

    /**
     * 构造器
     *
     * @param codes    消息编码
     * @param retry    一定时间内的业务事件处理失败时的重试次数，默认为3次
     * @param nextTime 失败时下次触发的间隔时间,单位：秒
     */
    protected SubscribeMsgListener(List<String> codes, Integer retry, Long nextTime) {
        this(null, codes, retry, nextTime, null);
    }

    /**
     * 构造器
     *
     * @param serviceId 消息服务的ID
     * @param codes     消息编码
     * @param retry     一定时间内的业务事件处理失败时的重试次数，默认为3次
     * @param nextTime  失败时下次触发的间隔时间,单位：秒
     */
    protected SubscribeMsgListener(String serviceId, List<String> codes, Integer retry, Long nextTime) {
        this(serviceId, codes, retry, nextTime, null);
    }

    /**
     * 构造器
     *
     * @param serviceId   消息服务的ID
     * @param codes       消息编码
     * @param retry       一定时间内的业务事件处理失败时的重试次数，默认为3次
     * @param nextTime    失败时下次触发的间隔时间,单位：秒
     * @param consumerNum 消费者数量
     */
    protected SubscribeMsgListener(String serviceId, List<String> codes, Integer retry, Long nextTime, Integer consumerNum) {
        super(retry, nextTime);
        this.consumerNum = consumerNum;
        if (null == serviceId || serviceId.trim().length() == 0) {
            serviceId = SpringUtil.getServiceId();
        }
        this.serviceId = (null == serviceId ? SpringUtil.getServiceId() : serviceId);
        this.codes = codes;
    }

    /**
     * 获取处理器
     */
    public List<SubMsgConsumer> getSubMsgConsumers() {
        if (null == serviceId) {
            return Collections.emptyList();
        }
        List<SubMsgConsumer> listeners = new ArrayList<>();
        for (String code : codes) {
            listeners.add(SubMsgConsumer.builder()
                    .listener(this)
                    .consumerNum(consumerNum)
                    .serviceId(serviceId)
                    .code(code)
                    .build());
        }
        return listeners;
    }
}
