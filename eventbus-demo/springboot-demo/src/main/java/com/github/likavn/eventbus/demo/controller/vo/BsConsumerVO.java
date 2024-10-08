package com.github.likavn.eventbus.demo.controller.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.github.likavn.eventbus.demo.entity.BsConsumer;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class BsConsumerVO extends BsConsumer {
    /**
     * 消息所属来源服务ID,服务名
     */
    private String serviceId;

    /**
     * 消息类型，用于区分不同的消息类型
     */
    private String code;

    /**
     * 消息体，必须包含无参构造函数
     */
    private String body;

    /**
     * 发送者IP
     */
    private String sendIpAddress;

    /**
     * 消息类型,1及时消息、2延时消息
     */
    private String typeStr;

    /**
     * 消息接收状态：0待处理、1处理成功、2处理失败
     */
    private String statusStr;
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dataCreateTime;
}
