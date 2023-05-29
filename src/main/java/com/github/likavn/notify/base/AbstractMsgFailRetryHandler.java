package com.github.likavn.notify.base;

import com.github.likavn.notify.annotation.FailCallback;
import com.github.likavn.notify.api.DelayMsgListener;
import com.github.likavn.notify.api.MsgSender;
import com.github.likavn.notify.api.SubscribeMsgListener;
import com.github.likavn.notify.domain.Message;
import com.github.likavn.notify.domain.Request;
import com.github.likavn.notify.prop.NotifyProperties;
import com.github.likavn.notify.utils.SpringUtil;
import com.github.likavn.notify.utils.WrapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;

import static com.github.likavn.notify.utils.WrapUtils.currentModelClass;
import static com.github.likavn.notify.utils.WrapUtils.parseObject;

/**
 * 消息投递失败处理类
 * 1、重复投递；
 * 2、重复投递失败，异常调用；
 *
 * @author likavn
 * @since 2023/01/01
 */
@SuppressWarnings("all")
public abstract class AbstractMsgFailRetryHandler<T> implements DelayMsgListener<T> {
    private static final Logger logger = LoggerFactory.getLogger(AbstractMsgFailRetryHandler.class);

    @Resource
    private MsgSender msgSender;

    @Resource
    private NotifyProperties properties;

    /**
     * 一定时间内的业务事件处理失败时的重试次数，默认为3次
     */
    private final Integer retry;

    /**
     * 失败时下次触发的间隔时间,单位：秒
     */
    private final Long nextTime;

    public AbstractMsgFailRetryHandler(Integer retry, Long nextTime) {
        this.retry = retry;
        this.nextTime = nextTime;
    }

    /**
     * 接收
     *
     * @param body body
     */
    public void deliver(String body) {
        deliver(WrapUtils.convertByBytes(body));
    }

    /**
     * 接收
     *
     * @param body body
     */
    public void deliver(byte[] body) {
        deliver(WrapUtils.convertByBytes(body));
    }

    /**
     * 接收器
     *
     * @param message message
     */
    public void deliver(Request request) {
        if (logger.isDebugEnabled()) {
            logger.debug("deliver msg：{}", WrapUtils.toJson(request));
        }
        Object originBody = request.getBody();

        // serialize Body
        serializeBody(request);
        try {
            accept(request);
        } catch (Throwable ex) {
            logger.error("deliver 业务异常", ex);
            int retryHandleNum = request.getDeliverNum();
            if (retryHandleNum < (null == retry ? properties.getFail().getRetryNum() : this.retry)) {
                logger.error("deliver 放入延时队列进行重试... ");

                // 设置延时消息回调处理类
                Class<? extends DelayMsgListener> delayMsgHandler = request.getDelayMsgHandler();
                long triggerTime = (null == nextTime ? properties.getFail().getDelayNextTime() : nextTime);
                // 原消息为订阅消息
                if (Boolean.TRUE.equals(request.getIsOrgSub())) {
                    delayMsgHandler = this.getClass();
                    triggerTime = (null == nextTime ? properties.getFail().getSubNextTime() : nextTime);
                }
                msgSender.sendDelayMessage(Request.builder().delayMsgHandler(delayMsgHandler)
                        .requestId(request.getRequestId())
                        .deliverNum(retryHandleNum + 1)
                        .delayTime(triggerTime)
                        .code(request.getCode())
                        .body(originBody)
                        .isOrgSub(request.getIsOrgSub())
                        .build());
            } else {
                try {
                    Object _this = SpringUtil.getBean(request.getDelayMsgHandler());
                    // 订阅消息获取其实现对象
                    if (Boolean.TRUE.equals(request.getIsOrgSub())) {
                        _this = this;
                    }
                    FailCallback.Error.onFail(_this, request, ex);
                } catch (Exception var2) {
                    logger.error("Error.onFail 业务异常", var2);
                }
            }
        }
    }

    /**
     * 序列化数据实体
     *
     * @param request
     */
    private void serializeBody(Request request) {
        Class modelClass = null;
        Class<? extends DelayMsgListener> delayMsgHandler = request.getDelayMsgHandler();
        if (null != delayMsgHandler) {
            DelayMsgListener delayMsgListener = SpringUtil.getBean(delayMsgHandler);
            modelClass = currentModelClass(delayMsgListener.getClass(), DelayMsgListener.class);
        } else {
            modelClass = currentModelClass(this.getClass(), SubscribeMsgListener.class);
        }
        request.setBody(parseObject(request.getBody(), modelClass));
    }

    /**
     * 订阅消息，重试才会回调此方法
     *
     * @param message 消息体
     */
    @Override
    public void onMessage(Message<T> message) {
        deliver((Request) message);
    }

    /**
     * 数据接收
     *
     * @param message 接收消息实体
     */
    public abstract void accept(Message<T> message);

}
