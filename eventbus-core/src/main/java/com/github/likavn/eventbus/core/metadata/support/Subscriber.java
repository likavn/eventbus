package com.github.likavn.eventbus.core.metadata.support;

import com.github.likavn.eventbus.core.metadata.MsgType;
import com.github.likavn.eventbus.core.utils.Func;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author likavn
 * @date 2024/01/01
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
     * 消息类型
     */
    private MsgType type = MsgType.TIMELY;
    /**
     * 投递触发
     */
    private Trigger trigger;
    /**
     * 失败触发
     */
    private FailTrigger failTrigger;

    public Subscriber(String serviceId, String code, MsgType type) {
        this.serviceId = serviceId;
        this.code = code;
        this.type = type;
    }

    public String getTopic() {
        return Func.getTopic(serviceId, code);
    }
}
