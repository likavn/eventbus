package com.github.likavn.notify.api;

import com.github.likavn.notify.domain.Message;
import com.github.likavn.notify.domain.SubMsgConsumer;
import com.github.likavn.notify.utils.SpringUtil;
import com.github.likavn.notify.utils.WrapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 消息订阅超类
 *
 * @author likavn
 * @since 2023/01/01
 */
public abstract class SubscribeMsgListener<T> implements DelayMsgListener<T> {
    private static final Logger logger = LoggerFactory.getLogger(SubscribeMsgListener.class);

    @Resource
    private MsgSender msgSender;

    /**
     * 失败时下次触发的间隔时间,单位：毫秒，默认半十分钟
     */
    private long triggerTime = 1000 * 60 * 10L;

    /**
     * 一定时间内的业务事件处理失败时的重试次数，默认为3次
     */
    private int retry = 3;

    /**
     * 消费者数量
     */
    private int consumerNum = 2;

    /**
     * 消息所属来源服务ID,服务名
     */
    private final String serviceId;

    /**
     * 消息类型，用于区分不同的消息类型
     */
    private final String[] codes;

    /**
     * 构造器
     *
     * @param codes 消息编码
     */
    protected SubscribeMsgListener(List<String> codes) {
        this(null, codes.toArray(new String[0]));
    }

    /**
     * 构造器
     *
     * @param serviceId 消息服务的ID
     * @param codes     消息编码
     */
    protected SubscribeMsgListener(String serviceId, String... codes) {
        if (null == serviceId || serviceId.trim().length() == 0) {
            serviceId = SpringUtil.getServiceId();
        }
        this.serviceId = serviceId;
        this.codes = codes;
    }

    /**
     * 构造器
     *
     * @param triggerTime 失败时下次触发的间隔时间,单位：秒
     * @param serviceId   消息服务的ID
     * @param codes       消息编码
     */
    protected SubscribeMsgListener(long triggerTime, String serviceId, String... codes) {
        this(serviceId, codes);
        this.triggerTime = triggerTime;
    }

    /**
     * 构造器
     *
     * @param triggerTime 失败时下次触发的间隔时间,单位：秒
     * @param retry       一定时间内的业务事件处理失败时的重试次数，默认为3次
     * @param serviceId   消息服务的ID
     * @param codes       消息编码
     */
    protected SubscribeMsgListener(long triggerTime, int retry, String serviceId, String... codes) {
        this(serviceId, codes);
        this.triggerTime = triggerTime;
        this.retry = retry;
    }

    /**
     * 构造器
     *
     * @param triggerTime 失败时下次触发的间隔时间,单位：秒
     * @param retry       一定时间内的业务事件处理失败时的重试次数，默认为3次
     * @param consumerNum 消费者数量
     * @param serviceId   消息服务的ID
     * @param codes       消息编码
     */
    protected SubscribeMsgListener(long triggerTime, int retry, int consumerNum, String serviceId, String... codes) {
        this(serviceId, codes);
        this.triggerTime = triggerTime;
        this.retry = retry;
        this.consumerNum = consumerNum;
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

    /**
     * 接收器
     *
     * @param message message
     */
    public void receiver(Message<T> message) {
        if (logger.isDebugEnabled()) {
            logger.debug("SubscribeMsgListener.receiver msg：{}", WrapUtils.toJson(message));
        }
        try {
            accept(message);
        } catch (Exception ex) {
            logger.error("SubscribeMsgListener.receiver 业务异常", ex);
            int retryHandleNum = message.getDeliverNum();
            if (retryHandleNum < retry) {
                msgSender.sendDelayMessage(this.getClass(), message.getBody(),
                        retryHandleNum + 1,
                        // 下次重试时间
                        triggerTime);
            }
        }
    }

    @Override
    public void onMessage(Message<T> message) {
        receiver(message);
    }

    /**
     * 数据接收
     *
     * @param message 接收消息实体
     */
    public abstract void accept(Message<T> message);

}
