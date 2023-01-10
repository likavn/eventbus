package com.github.likavn.notify.prop;

import com.github.likavn.notify.domain.SubMsgListener;
import lombok.Data;

import java.util.List;

/**
 * 配置
 *
 * @author likavn
 * @since 2023/01/01
 */
@Data
public class NotifyProperties {

    /**
     * 服务ID
     */
    private String serviceId;

    /**
     * 订阅器
     */
    List<SubMsgListener> subMsgListeners;
}
