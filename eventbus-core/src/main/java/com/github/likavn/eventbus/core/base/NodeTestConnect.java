package com.github.likavn.eventbus.core.base;

/**
 * 节点连接状态检测接口，用于判断当前应用是否与节点断开连接
 *
 * @author likavn
 * @date 2023/5/19
 **/
public interface NodeTestConnect {

    /**
     * 检测确认节点是否连接
     *
     * @return true已连接、false连接断开
     */
    boolean testConnect();
}
