package com.github.likavn.eventbus.test.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.likavn.eventbus.core.utils.Func;
import com.github.likavn.eventbus.test.controller.vo.BsConsumerVO;
import com.github.likavn.eventbus.test.entity.BsConsumer;
import com.github.likavn.eventbus.test.enums.ConsumerStatus;
import com.github.likavn.eventbus.test.mapper.BsConsumerMapper;
import org.springframework.stereotype.Service;

@Service
public class BsConsumerServiceImpl extends ServiceImpl<BsConsumerMapper, BsConsumer> implements BsConsumerService {

    @Override
    public IPage<BsConsumerVO> selectPage(IPage<BsConsumerVO> page, BsConsumerVO consumer) {
        IPage<BsConsumerVO> retPage = baseMapper.listPage(page, consumer);
        if (!Func.isEmpty(retPage.getRecords())) {
            retPage.getRecords().forEach(item -> {
                item.setTypeStr(Integer.valueOf(1).equals(item.getType()) ? "及时消息" : "延时消息");
                ConsumerStatus consumerStatus = ConsumerStatus.of(item.getStatus());
                if (null != consumerStatus) {
                    item.setStatusStr(consumerStatus.getName());
                }
            });
        }
        return retPage;
    }
}
