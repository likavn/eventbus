package com.github.likavn.eventbus.core.metadata.data;

/**
 * 业务消息体
 * 发送消息时避免每次都要写code编码
 *
 * @author likavn
 * @date 2024/04/19
 */
public abstract class AbstractBody {

    /**
     * 消息体code
     *
     * @return code编码
     */
    public abstract String code();
}
