package com.github.likavn.notify.domain;

import lombok.Data;

import java.util.List;

/**
 * 服务元数据
 *
 * @author likavn
 * @date 2023/2/22
 **/
@Data
public class ServiceContext {

    /**
     * 服务ID
     */
    private String serviceId;

    /**
     * 订阅器
     */
    private List<SubMsgConsumer> subMsgConsumers;
}
