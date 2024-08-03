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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author likavn
 * @date 2024/01/01
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Listener {
    /**
     * 消息所属来源服务ID,服务名
     */
    private String serviceId;
    /**
     * 消息编码
     */
    private String code;

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

    public Listener(String serviceId, String code, int concurrency, MsgType type) {
        this.serviceId = serviceId;
        this.code = code;
        this.concurrency = concurrency;
        this.type = type;
    }


    public void isValid() {
        Polling.ValidatorInterval.isValid(null == polling ? null : polling.interval());
        if (null != toDelay) {
            Assert.isTrue(toDelay.delayTime() > 0, "@ToDelay.delayTime must be greater than 0");
        }
    }

    public static Listener ofDelay(BusConfig config) {
        return new Listener(config.getServiceId(), null, config.getDelayConcurrency(), MsgType.DELAY);
    }

    public String getTopic() {
        return Func.getTopic(serviceId, code);
    }
}
