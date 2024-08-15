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
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息数据
 *
 * @author likavn
 * @date 2024/3/31
 **/
@Data
@Builder
@TableName("bs_data")
public class BsData {
    /**
     * 事件ID,默认UUID
     */
    @TableId
    private String requestId;

    /**
     * 消息所属来源服务ID,服务名
     */
    private String serviceId;

    /**
     * 消息类型，用于区分不同的消息类型
     */
    private String code;

    /**
     * 消息类型,1及时消息、2延时消息
     */
    private Integer type;

    /**
     * 消息体，必须包含无参构造函数
     */
    private String body;

    /**
     * 发送者IP
     */
    private String ipAddress;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
