package com.github.likavn.eventbus.demo.controller;

import com.github.likavn.eventbus.core.api.MsgSender;
import com.github.likavn.eventbus.core.base.MsgListenerContainer;
import com.github.likavn.eventbus.core.utils.Assert;
import com.github.likavn.eventbus.demo.constant.MsgConstant;
import com.github.likavn.eventbus.demo.domain.R;
import com.github.likavn.eventbus.demo.domain.TMsg;
import com.github.likavn.eventbus.demo.domain.TestBody;
import com.github.likavn.eventbus.demo.listener.DemoMsgDelayListener;
import com.github.likavn.eventbus.demo.service.BsHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.websocket.server.PathParam;

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

    @Resource
    private MsgListenerContainer msgListenerContainer;

    @Resource
    private BsHelper bsHelper;

    /**
     * 测试消息
     */
    @PostMapping(value = "/trigger/{type}/{count}")
    public R<Boolean> trigger(@PathVariable("type") Integer type,
                              @PathVariable("count") Long count, @PathParam("delayTime") Long delayTime, @RequestBody String content) {
        try {
            long l = System.currentTimeMillis();
            log.info("发送消息数量count={}条,msg={}", count, content);
            Assert.notEmpty(content, "msg不能为空");
            for (int i = 0; i < count; i++) {
                switch (type) {
                    case 1:
                        TestBody testBody = new TestBody();
                        testBody.setContent(content);
                        msgSender.send(testBody);
                        break;
                    case 2:
                        TMsg msg2 = new TMsg();
                        msg2.setContent(content);
                        msgSender.send(MsgConstant.TEST_MSG_SUBSCRIBE, msg2);
                        break;
                    case 3:

                        TMsg msg3 = new TMsg();
                        msg3.setContent(content);
                        msgSender.sendDelayMessage(DemoMsgDelayListener.class, msg3, delayTime);
                        break;
                    case 4:
                        TMsg msg4 = new TMsg();
                        msg4.setContent(content);
                        msgSender.sendDelayMessage(MsgConstant.TEST_MSG_DELAY_SUBSCRIBE, msg4, delayTime);
                        break;
                    default:
                        log.error("发送失败...");
                }
            }
            log.info("发送成功，耗时{}ms...", System.currentTimeMillis() - l);
            return R.ok(Boolean.TRUE);
        } catch (Exception e) {
            log.error("DemoController.trigger", e);
            return R.fail(e.getMessage());
        }
    }

    @GetMapping(value = "/active")
    public R<Boolean> active() {
        return R.ok(msgListenerContainer.isActive());
    }

    @GetMapping(value = "/start")
    public R<Boolean> start() {
        try {
            msgListenerContainer.startup();
            return R.ok(Boolean.TRUE);
        } catch (Exception e) {
            log.error("DemoController.start", e);
            return R.fail(e.getMessage());
        }
    }

    @GetMapping(value = "/stop")
    public R<Boolean> stop() {
        try {
            msgListenerContainer.shutdown();
            return R.ok(Boolean.TRUE);
        } catch (Exception e) {
            log.error("DemoController.stop", e);
            return R.fail(e.getMessage());
        }
    }

    @GetMapping(value = "/reSendMessage")
    public R<Boolean> reSendMessage(@RequestParam("consumerId") Long consumerId) {
        bsHelper.reSendMessage(consumerId);
        return R.ok(Boolean.TRUE);
    }
}
