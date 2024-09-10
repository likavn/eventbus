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
package com.github.likavn.eventbus;

import com.github.likavn.eventbus.core.api.MsgSender;
import com.github.likavn.eventbus.test.EventBusApplication;
import com.github.likavn.eventbus.test.domain.TMsg;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * json测试
 *
 * @author likavn
 * @date 2024/5/23
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = EventBusApplication.class)
public class MsgSenderTest {

    @Resource
    private MsgSender msgSender;

    @Test
    public void testArrayListener() {
        TMsg tMsg = new TMsg();
        tMsg.setId("123");
        TMsg tMsg1 = new TMsg();
        tMsg1.setId("4");
        msgSender.send("testArrayListener", new TMsg[]{tMsg, tMsg1});
    }

    @Test
    public void testBeanObjectListener() {
        TMsg tMsg = new TMsg();
        tMsg.setId("123");
        msgSender.send("testBeanObjectListener", tMsg);
    }

    @Test
    public void testIntegerListener() {
        msgSender.send("testIntegerListener", 123);
    }

    @Test
    public void testListListener() {
        List<TMsg> list = new ArrayList<>(2);
        TMsg tMsg = new TMsg();
        tMsg.setId("123");
        list.add(tMsg);
        TMsg tMsg1 = new TMsg();
        tMsg1.setId("4");
        list.add(tMsg1);
        msgSender.send("testListListener", list);
    }

    @Test
    public void testMapListener() {
        Map<String, Object> map = new HashMap<>(4);
        map.put("k1", 1);
        map.put("k2", "9");
        msgSender.send("testMapListener", map);
    }

    @Test
    public void testStringListener() {
        msgSender.send("testStringListener", "test");
    }
}
