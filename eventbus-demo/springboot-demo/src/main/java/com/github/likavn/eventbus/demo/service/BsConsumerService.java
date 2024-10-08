package com.github.likavn.eventbus.demo.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.github.likavn.eventbus.demo.controller.vo.BsConsumerVO;
import com.github.likavn.eventbus.demo.entity.BsConsumer;

public interface BsConsumerService extends IService<BsConsumer> {
    IPage<BsConsumerVO> selectPage(IPage<BsConsumerVO> page,BsConsumerVO consumer);
}
