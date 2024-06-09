package com.github.likavn.eventbus.core.metadata.data;

/**
 * 业务消息体
 * 发送消息时避免每次都要写code编码
 * <p>
 * 备注：实现类必须存在无参构造器
 *
 * @author likavn
 * @date 2024/04/19
 */
public interface MsgBody {

    /**
     * 消息体code
     *
     * @return code编码
     */
    String code();
}
