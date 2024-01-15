package com.github.likavn.eventbus.demo;

import com.github.likavn.eventbus.core.api.MsgSender;
import com.github.likavn.eventbus.demo.constant.MsgConstant;
import com.github.likavn.eventbus.demo.domain.TMsg;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 单体启动类 报错提醒: 未集成mongo报错，可以打开启动类上面的注释 exclude={MongoAutoConfiguration.class}
 */
@Slf4j
@RestController
@RequestMapping("/eventbus")
@SpringBootApplication
@ComponentScan(value = {"com.github.likavn.eventbus.demo.**"})
public class EventBusApplication extends SpringBootServletInitializer {
    @Resource
    private MsgSender msgSender;

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(EventBusApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(EventBusApplication.class, args);
        log.info("启动成功");
    }

    /**
     * 通过id查询
     */
    @GetMapping(value = "/trigger")
    public String queryById() {
        List<TMsg> msgs = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            TMsg request = new TMsg();
            request.setName("999121#" + i);
            request.setContent("kkss");
            request.setType(2);
            msgs.add(request);
        }

        msgs.parallelStream().forEach(msg -> {
            // 测试订阅消息
            msgSender.send(MsgConstant.TEST_MSG_SUBSCRIBE, msg);
            // 测试订阅消息，传递消息代码code
            //msgSender.send(MsgConstant.TEST_MSG_SUBSCRIBE_LISTENER, 1L);
            // msgSender.send(MsgConstant.TEST_MSG_SUBSCRIBE_LISTENER, msg);
            // 测试延时消息，直接关联处理类
            //msgSender.sendDelayMessage(DemoMsgDelayListener.class, msg, 15);
            // 测试延时消息，传递消息代码code
            //  msgSender.sendDelayMessage(MsgConstant.TEST_MSG_DELAY_SUBSCRIBE, msg, 10);
        });

        log.info("发送成功...");
        return "发送成功...";
    }
}
