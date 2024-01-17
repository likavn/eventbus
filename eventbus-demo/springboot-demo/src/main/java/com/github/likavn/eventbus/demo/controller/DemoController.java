package com.github.likavn.eventbus.demo.controller;

import com.github.likavn.eventbus.core.api.MsgSender;
import com.github.likavn.eventbus.demo.domain.TMsg;
import com.github.likavn.eventbus.demo.listener.DemoMsgDelayListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author likavn
 * @date 2024/1/15
 **/
@Slf4j
@RestController
@RequestMapping("/eventbus")
public class DemoController {

    @Resource
    private MsgSender msgSender;

    /**
     * 测试消息
     */
    @GetMapping(value = "/trigger")
    public String trigger() {
        List<TMsg> msgs = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            TMsg request = new TMsg();
            request.setName("999121#" + i);
            request.setContent("kkss");
            request.setType(2);
            msgs.add(request);
        }
        log.info("开始发送，数据{}条...", msgs.size());
        msgs.parallelStream().forEach(msg -> {
            // 测试订阅消息
            // msgSender.send(MsgConstant.TEST_MSG_SUBSCRIBE, msg);
            // 测试订阅消息，传递消息代码code
            //msgSender.send(MsgConstant.TEST_MSG_SUBSCRIBE_LISTENER, 1L);
            // msgSender.send(MsgConstant.TEST_MSG_SUBSCRIBE_LISTENER, msg);
            // 测试延时消息，直接关联处理类
            msgSender.sendDelayMessage(DemoMsgDelayListener.class, msg, 5);
            // 测试延时消息，传递消息代码code
            //  msgSender.sendDelayMessage(MsgConstant.TEST_MSG_DELAY_SUBSCRIBE, msg, 10);
        });

        log.info("发送成功...");
        return "发送成功...";
    }
}
