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
package com.github.likavn.eventbus.demo.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.likavn.eventbus.core.metadata.support.Trigger;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 消费者
 *
 * @author likavn
 * @date 2024/3/31
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("bs_consumer")
public class BsConsumer {
    /**
     * 数据ID
     */
    @TableId
    private String id;
    /**
     * 事件ID,默认UUID
     */
    private String requestId;
    /**
     * 消息接收处理器（消费者ID）ID=全类名+方法名{@link Trigger#getDeliverId()}
     */
    private String deliverId;

    /**
     * 消息投递次数
     */
    private Integer deliverCount;

    /**
     * 延时时间，单位：秒
     */
    private Long delayTime;

    /**
     * 消息类型,1及时消息、2延时消息
     */
    private Integer type;

    /**
     * 消息接收状态：0待处理、1处理成功、2处理失败
     */
    private Integer status;

    /**
     * 接收消息处理成功时间
     */
    private LocalDateTime successTime;

    /**
     * 发生异常的时间
     */
    private LocalDateTime exceptionTime;

    /**
     * 异常信息
     */
    private String exceptionMessage;

    /**
     * 异常信息堆栈信息
     */
    private String exceptionStackTrace;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
