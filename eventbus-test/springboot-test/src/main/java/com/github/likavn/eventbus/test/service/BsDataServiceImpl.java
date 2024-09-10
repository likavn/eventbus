package com.github.likavn.eventbus.test.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.likavn.eventbus.test.entity.BsData;
import com.github.likavn.eventbus.test.mapper.BsDataMapper;
import org.springframework.stereotype.Service;

@Service
public class BsDataServiceImpl extends ServiceImpl<BsDataMapper, BsData> implements BsDataService{
}
