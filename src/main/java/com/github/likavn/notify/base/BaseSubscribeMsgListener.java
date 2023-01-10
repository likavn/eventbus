package com.github.likavn.notify.base;

import com.github.likavn.notify.api.DelayMsgListener;
import com.github.likavn.notify.api.MsgSender;
import com.github.likavn.notify.domain.MsgRequest;
import com.github.likavn.notify.domain.SubMsgListener;
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
public abstract class BaseSubscribeMsgListener<T> implements DelayMsgListener<T> {
    private static final Logger logger = LoggerFactory.getLogger(BaseSubscribeMsgListener.class);

    @Resource
    private MsgSender msgSender;

    /**
     * 失败时下次触发的间隔时间,单位：毫秒，默认半十分钟
     */
    private long triggerTime = 1000 * 60 * 10L;

    /**
     * 一定时间内的业务事件处理失败时的重试次数，默认为5次
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
    protected BaseSubscribeMsgListener(List<String> codes) {
        this(SpringUtil.getAppName(), codes.toArray(new String[0]));
    }

    /**
     * 构造器
     *
     * @param serviceId 消息服务的ID
     * @param codes     消息编码
     */
    protected BaseSubscribeMsgListener(String serviceId, String... codes) {
        this.serviceId = serviceId;
        this.codes = codes;
    }

    /**
     * 构造器
     *
     * @param triggerTime 失败时下次触发的间隔时间,单位：秒
     * @param retry       一定时间内的业务事件处理失败时的重试次数，默认为3次
     * @param serviceId   消息服务的ID
     * @param codes       消息编码
     */
    protected BaseSubscribeMsgListener(long triggerTime, int retry, String serviceId, String... codes) {
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
    protected BaseSubscribeMsgListener(long triggerTime, int retry, int consumerNum, String serviceId, String... codes) {
        this(serviceId, codes);
        this.triggerTime = triggerTime;
        this.retry = retry;
        this.consumerNum = consumerNum;
    }

    /**
     * 获取处理器
     */
    public List<SubMsgListener> getSubMsgListeners() {
        if (null == serviceId) {
            return Collections.emptyList();
        }
        List<SubMsgListener> listeners = new ArrayList<>();
        for (String code : codes) {
            listeners.add(SubMsgListener.builder()
                    .listener(this)
                    .consumerNum(consumerNum)
                    .serviceId(serviceId)
                    .code(code)
                    .build());
        }
        return listeners;
    }

    /**
     * 接收
     *
     * @param msgRequest bean
     */
    public void receiverDelivery(MsgRequest<T> msgRequest) {
        // 设置处理器
        receiver(msgRequest);
    }

    /**
     * 接收器
     *
     * @param msgRequest msgRequest
     */
    private void receiver(MsgRequest<T> msgRequest) {
        if (logger.isDebugEnabled()) {
            logger.debug("BaseMsgReceiver.receiver msg：{}", WrapUtils.toJson(msgRequest));
        }
        try {
            accept(msgRequest.getBody());
        } catch (Exception ex) {
            logger.error("BaseMsgReceiver.receiver 业务异常", ex);
            int retryHandleNum = null == msgRequest.getHandlerNum() ? 1 : msgRequest.getHandlerNum();
            if (retryHandleNum < retry) {
                msgSender.sendDelayMessage(MsgRequest.builder()
                                .handler(this.getClass())
                                .body(msgRequest.getBody())
                                .handlerNum(retryHandleNum + 1)
                                .build(),
                        // 下次重试时间
                        triggerTime);
            }
        }
    }

    @Override
    public void onMessage(MsgRequest<T> event) {
        receiver(event);
    }

    /**
     * 数据接收
     *
     * @param t 接收实体
     */
    public abstract void accept(T t);

}
