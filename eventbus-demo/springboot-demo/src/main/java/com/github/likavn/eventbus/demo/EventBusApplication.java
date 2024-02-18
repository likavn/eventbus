package com.github.likavn.eventbus.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * EventBusDemo
 *
 * @author nnnmar
 */
@Slf4j
@SpringBootApplication
@EnableAspectJAutoProxy(exposeProxy = true)
@SuppressWarnings("all")
public class EventBusApplication extends SpringApplication {
    private static final EventBusApplication that = new EventBusApplication(EventBusApplication.class);

    public EventBusApplication(Class<?>... primarySources) {
        super(primarySources);
        setBannerMode(Banner.Mode.LOG);
    }

    public static void main(String[] args) {
        that.run(args);
        log.info("启动成功");
    }
}
