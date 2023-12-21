package com.github.likavn.eventbus;

import com.github.likavn.eventbus.core.SubscriberRegistry;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

/**
 * @author likavn
 * @date 2023/12/20
 **/
public class SubscriberBootRegistry implements ApplicationRunner {
    @Override
    public void run(ApplicationArguments args) {
        SubscriberRegistry.register(this);
    }
}
