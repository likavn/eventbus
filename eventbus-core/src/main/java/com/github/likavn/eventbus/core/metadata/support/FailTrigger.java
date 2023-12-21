package com.github.likavn.eventbus.core.metadata.support;

import com.github.likavn.eventbus.core.annotation.Fail;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 消息投递失败触发器
 *
 * @author likavn
 * @date 2023/12/20
 **/
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FailTrigger extends Trigger {
    /**
     * 投递失败配置信息
     */
    private Fail fail;

    public FailTrigger(Fail fail, Trigger trigger) {
        super(trigger.getInvokeBean(), trigger.getMethod(), trigger.getArgTypes());
        this.fail = fail;
    }
}
