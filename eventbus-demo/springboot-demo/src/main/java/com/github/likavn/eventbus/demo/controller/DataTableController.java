package com.github.likavn.eventbus.demo.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.likavn.eventbus.demo.controller.vo.BsConsumerVO;
import com.github.likavn.eventbus.demo.domain.R;
import com.github.likavn.eventbus.demo.service.BsConsumerService;
import com.github.likavn.eventbus.demo.helper.BsHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author likavn
 * @date 2024/1/15
 **/
@Slf4j
@RestController
@RequestMapping("/eventbus/table")
public class DataTableController {
    @Lazy
    @Resource
    private BsConsumerService bsConsumerService;

    @Resource
    private BsHelper bsHelper;

    @PostMapping(value = "/reSendMessage")
    public R<Boolean> reSendMessage(@RequestParam("consumerDataId") Long consumerDataId) {
        bsHelper.reSendMessage(consumerDataId);
        return R.ok(Boolean.TRUE);
    }

    @GetMapping(value = "/page")
    public R<IPage<BsConsumerVO>> selectPage(Page<BsConsumerVO> page, BsConsumerVO consumer) {
        return R.ok(bsConsumerService.selectPage(page, consumer));
    }
}
