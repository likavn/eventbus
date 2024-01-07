package com.github.likavn.eventbus.core.metadata.support;

import com.github.likavn.eventbus.core.annotation.Fail;
import lombok.Getter;

/**
 * 消息投递失败触发器
 *
 * @author likavn
 * @date 2024/01/01
 **/
@Getter
public class FailTrigger extends Trigger {
    /**
     * 投递失败配置信息
     */
    private Fail fail;

    public FailTrigger(Fail fail, Trigger trigger) {
        super(trigger.getInvokeBean(), trigger.getMethod());
        this.fail = fail;
    }
}
