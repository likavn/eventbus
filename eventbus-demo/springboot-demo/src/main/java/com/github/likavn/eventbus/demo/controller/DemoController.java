package com.github.likavn.eventbus.demo.controller;

import com.github.likavn.eventbus.core.api.MsgSender;
import com.github.likavn.eventbus.core.base.MsgListenerContainer;
import com.github.likavn.eventbus.core.utils.Assert;
import com.github.likavn.eventbus.demo.constant.MsgConstant;
import com.github.likavn.eventbus.demo.domain.R;
import com.github.likavn.eventbus.demo.listener.DemoMsgDelayListener;
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

    /**
     * 测试消息
     */
    @PostMapping(value = "/trigger/{type}/{count}")
    public R<Boolean> trigger(@PathVariable("type") Integer type,
                              @PathVariable("count") Long count, @PathParam("delayTime") Long delayTime, @RequestBody String msg) {
        try {
            log.info("发送消息数量count={}条,msg={}", count, msg);
            Assert.notEmpty(msg, "msg不能为空");
            for (int i = 0; i < count; i++) {
                switch (type) {
                    case 1:
                        msgSender.send(MsgConstant.TEST_MSG_SUBSCRIBE, msg);
                        break;
                    case 2:
                        msgSender.send(MsgConstant.TEST_MSG_SUBSCRIBE_LISTENER, msg);
                        break;
                    case 3:
                        msgSender.sendDelayMessage(MsgConstant.TEST_MSG_DELAY_SUBSCRIBE, msg, delayTime);
                        break;
                    case 4:
                        msgSender.sendDelayMessage(DemoMsgDelayListener.class, msg, delayTime);
                        break;
                    default:
                        log.error("发送失败...");
                }
            }
            log.info("发送成功...");
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
}
