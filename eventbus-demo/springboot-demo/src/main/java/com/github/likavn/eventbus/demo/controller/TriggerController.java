package com.github.likavn.eventbus.demo.controller;

import com.github.likavn.eventbus.core.api.MsgSender;
import com.github.likavn.eventbus.core.base.MsgListenerContainer;
import com.github.likavn.eventbus.core.utils.Assert;
import com.github.likavn.eventbus.demo.constant.MsgConstant;
import com.github.likavn.eventbus.demo.domain.R;
import com.github.likavn.eventbus.demo.domain.TMsg;
import com.github.likavn.eventbus.demo.domain.TestBody;
import com.github.likavn.eventbus.demo.domain.TestDelayBody;
import com.github.likavn.eventbus.demo.helper.BsHelper;
import com.github.likavn.eventbus.demo.listener.MsgDelayListenerClassName;
import com.github.likavn.eventbus.demo.listener.MsgListenerClassName;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.websocket.server.PathParam;
import java.util.concurrent.*;

/**
 * @author likavn
 * @date 2024/1/15
 **/
@Slf4j
@RestController
@RequestMapping("/eventbus")
public class TriggerController {

    @Lazy
    @Resource
    private MsgSender msgSender;

    @Lazy
    @Resource
    private MsgListenerContainer msgListenerContainer;

    @Resource
    private BsHelper bsHelper;

    private final ExecutorService executorService = new ThreadPoolExecutor(
            10,
            100,
            0L,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(1000),
            new ThreadPoolExecutor.CallerRunsPolicy());

    /**
     * 测试消息
     */
    @PostMapping(value = "/trigger/{type}/{count}")
    public R<String> trigger(@PathVariable("type") Integer type,
                             @PathVariable("count") Long count, @PathParam("delayTime") Long delayTime, @RequestBody String content) {
        try {
            long l = System.currentTimeMillis();
            log.info("发送消息数量count={}条,msg={}", count, content);
            Assert.notEmpty(content, "msg不能为空");
            CountDownLatch latch = new CountDownLatch(Math.toIntExact(count));
            for (int i = 0; i < count; i++) {
                executorService.execute(() -> {
                    switch (type) {
                        case 1:
                            TestBody testBody = new TestBody();
                            testBody.setContent(content);
                            msgSender.send(testBody);
                            break;
                        case 2:
                            TMsg msg2 = new TMsg();
                            msg2.setContent(content);
                            msgSender.send(MsgConstant.MSG_LISTENER_CODE, msg2);
                            break;
                        case 3:
                            msgSender.send(MsgListenerClassName.class, content);
                            break;
                        case 11:
                            TestDelayBody msg3 = new TestDelayBody();
                            msg3.setContent(content);
                            msgSender.sendDelayMessage(msg3, delayTime);
                            break;
                        case 12:
                            TMsg msg4 = new TMsg();
                            msg4.setContent(content);
                            msgSender.sendDelayMessage(MsgConstant.MSG_DELAY_LISTENER_CODE, msg4, delayTime);
                            break;
                        case 13:
                            msgSender.sendDelayMessage(MsgDelayListenerClassName.class, content, delayTime);
                            break;
                        default:
                            log.error("发送失败...");
                    }
                    latch.countDown();
                });
            }
            latch.await();
            String logStr = String.format("发送成功，耗时%s ms...", (System.currentTimeMillis() - l));
            log.info(logStr);
            return R.ok(logStr);
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
