package com.github.likavn.eventbus.core.metadata.support;

import com.github.likavn.eventbus.core.utils.Func;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author likavn
 * @date 2023/12/17
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Subscriber {
    /**
     * 消息所属来源服务ID,服务名
     */
    private String serviceId;
    /**
     * 消息编码
     */
    private String code;
    /**
     * 是否是延时消息
     */
    private boolean delayMsg;
    /**
     * 投递触发
     */
    private Trigger trigger;
    /**
     * 失败触发
     */
    private FailTrigger failTrigger;

    public Subscriber(String serviceId, String code, boolean delayMsg) {
        this.serviceId = serviceId;
        this.code = code;
        this.delayMsg = delayMsg;
    }

    public String getTopic() {
        return Func.getTopic(serviceId, code);
    }
}
