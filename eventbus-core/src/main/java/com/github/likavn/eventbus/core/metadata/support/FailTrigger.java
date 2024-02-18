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

import com.github.likavn.eventbus.core.annotation.Fail;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * 消息投递失败触发器
 *
 * @author likavn
 * @date 2024/01/01
 **/
@Getter
@EqualsAndHashCode(callSuper = true)
public class FailTrigger extends Trigger {
    /**
     * 投递失败配置信息
     */
    private final Fail fail;

    public FailTrigger(Fail fail, Trigger trigger) {
        super(trigger.getInvokeBean(), trigger.getMethod());
        this.fail = fail;
    }
}
