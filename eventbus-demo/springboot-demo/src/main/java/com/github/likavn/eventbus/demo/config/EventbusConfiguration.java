/**
 * Copyright 2023-2033, likavn (likavn@163.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.likavn.eventbus.demo.config;

import com.baomidou.mybatisplus.core.toolkit.Sequence;
import com.github.likavn.eventbus.core.api.RequestIdGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author likavn
 * @date 2024/4/2
 **/
@Configuration
public class EventbusConfiguration {

    @Bean
    public RequestIdGenerator requestIdGenerator() throws UnknownHostException {
        Sequence sequence = new Sequence(InetAddress.getLocalHost());
        return () -> String.valueOf(sequence.nextId());
    }
}