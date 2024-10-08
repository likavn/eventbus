package com.github.likavn.eventbus.demo.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.likavn.eventbus.demo.entity.BsData;
import com.github.likavn.eventbus.demo.mapper.BsDataMapper;
import org.springframework.stereotype.Service;

@Service
public class BsDataServiceImpl extends ServiceImpl<BsDataMapper, BsData> implements BsDataService{
}
