package com.github.likavn.eventbus.test.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.github.likavn.eventbus.test.controller.vo.BsConsumerVO;
import com.github.likavn.eventbus.test.entity.BsConsumer;

public interface BsConsumerService extends IService<BsConsumer> {
    IPage<BsConsumerVO> selectPage(IPage<BsConsumerVO> page,BsConsumerVO consumer);
}
