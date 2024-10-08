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
package com.github.likavn.eventbus.core.metadata.support;

import com.github.likavn.eventbus.core.annotation.Polling;
import com.github.likavn.eventbus.core.annotation.ToDelay;
import com.github.likavn.eventbus.core.metadata.BusConfig;
import com.github.likavn.eventbus.core.metadata.MsgType;
import com.github.likavn.eventbus.core.utils.Assert;
import com.github.likavn.eventbus.core.utils.Func;
import lombok.Data;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 监听器元数据
 *
 * @author likavn
 * @date 2024/01/01
 **/
@Data
public class Listener {
    /**
     * 消息所属来源服务ID,服务名
     */
    private String serviceId;
    /**
     * 消息编码
     */
    private List<String> codes;

    /**
     * 定义并发级别，默认{@link BusConfig#getConcurrency()}。
     */
    private int concurrency;
    /**
     * 监听器类型
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

    /**
     * 轮询配置
     */
    private Polling polling;

    /**
     * 异步消息转延时消息配置
     */
    private ToDelay toDelay;

    public Listener(String serviceId, List<String> codes, int concurrency, MsgType type) {
        this.serviceId = serviceId;
        this.codes = codes;
        this.concurrency = concurrency;
        this.type = type;
    }

    public Listener(String serviceId, List<String> codes, int concurrency, Trigger trigger, FailTrigger failTrigger, Polling polling) {
        this.serviceId = serviceId;
        this.codes = codes;
        this.concurrency = concurrency;
        this.trigger = trigger;
        this.failTrigger = failTrigger;
        this.polling = polling;
    }

    public void isValid() {
        if (null != polling) {
            Polling.ValidatorInterval.isValid(polling.interval());
        }
        if (null != toDelay) {
            Assert.isTrue(toDelay.delayTime() > 0, "@ToDelay.delayTime must be greater than 0");
        }
    }

    /**
     * 投递ID
     */
    public String getDeliverId() {
        return trigger.getDeliverId();
    }

    public List<String> getTopics() {
        if (Func.isEmpty(codes)) {
            return Collections.emptyList();
        }
        return codes.stream().map(code -> Func.getTopic(serviceId, code)).collect(Collectors.toList());
    }
}
