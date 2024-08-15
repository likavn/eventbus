-- eventbus.bs_data definition
DROP TABLE if EXISTS bs_data;
CREATE TABLE `bs_data`
(
    `request_id`  varchar(64)  NOT NULL COMMENT '事件ID/消息ID,默认UUID,可自定义实现接口（RequestIdGenerator）获取消息的ID',
    `service_id`  varchar(100) NOT NULL COMMENT '消息所属来源服务ID/服务名',
    `code`        varchar(256) DEFAULT NULL COMMENT '消息编码',
    `type`        int(1) DEFAULT NULL COMMENT '消息类型,1及时消息、2延时消息',
    `body`        text COMMENT '消息数据的JSON串',
    `ip_address`  varchar(32)  DEFAULT NULL COMMENT '消息发送者IP地址',
    `create_time` datetime     DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (`request_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='eventbus数据表';

-- eventbus.bs_consumer definition
DROP TABLE if EXISTS bs_consumer;
CREATE TABLE `bs_consumer`
(
    `id`                    bigint(20) NOT NULL COMMENT 'id',
    `request_id`            varchar(64)  NOT NULL COMMENT '事件ID/消息ID,默认UUID,可自定义实现接口（RequestIdGenerator）获取消息的ID',
    `type`                  int(1) DEFAULT NULL COMMENT '消息类型,1及时消息、2延时消息',
    `deliver_id`            varchar(256) NOT NULL COMMENT '消息接收处理器（消费者ID）ID=全类名+方法名{@link Trigger#getDeliverId()}',
    `deliver_count`         int(11) DEFAULT NULL COMMENT '消息投递次数',
    `ip_address`            varchar(32)   DEFAULT NULL COMMENT '消息接收者IP地址',
    `polling_count`         int(11) DEFAULT NULL COMMENT '消费者轮询次数',
    `fail_retry_count`      int(11) DEFAULT NULL COMMENT '消费者接收失败后时，发起失败重试的次数',
    `delay_time`            bigint(20) DEFAULT NULL COMMENT '延时时间，单位：秒',
    `to_delay`              int(11) DEFAULT NULL COMMENT '是否已转为延迟消息,0否、1是',
    `status`                int(1) DEFAULT NULL COMMENT '消息接收状态：0待处理、1处理成功、2处理失败',
    `success_time`          datetime      DEFAULT NULL COMMENT '接收消息处理成功时间',
    `exception_time`        datetime      DEFAULT NULL COMMENT '发生异常的时间',
    `exception_message`     varchar(1000) DEFAULT NULL COMMENT '异常信息',
    `exception_stack_trace` text COMMENT '异常堆栈信息',
    `create_time`           datetime      DEFAULT NULL COMMENT '创建时间',
    `update_time`           datetime      DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY                     `bs_consumer_request_id_IDX` (`request_id`,`deliver_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='eventbus消费者';