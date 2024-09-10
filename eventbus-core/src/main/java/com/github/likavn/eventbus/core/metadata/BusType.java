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
package com.github.likavn.eventbus.core.metadata;

import com.github.likavn.eventbus.core.constant.BusConstant;
import com.github.likavn.eventbus.core.utils.Assert;

/**
 * 消息引擎类别（redis、rabbitmq、rocketmq）
 *
 * @author likavn
 * @date 2023/03/06
 * @since 2.3.4
 **/
public enum BusType {
    /**
     * redis
     */
    REDIS(BusConstant.REDIS),
    RABBITMQ(BusConstant.RABBITMQ),
    ROCKETMQ(BusConstant.ROCKETMQ),
    ;

    private final String name;

    BusType(String name) {
        this.name = name;
    }

    public boolean valid(String name) {
        return name().equals(name);
    }

    public static BusType of(String name) {
        for (BusType value : values()) {
            if (value.name.equals(name)) {
                return value;
            }
        }
        return null;
    }

    public static void isValid(String name) {
        BusType busType = BusType.of(name);
        Assert.notNull(busType, "Eventbus type is not supported: " + name);
    }
}
