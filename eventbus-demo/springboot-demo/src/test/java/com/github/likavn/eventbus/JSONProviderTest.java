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

import com.github.likavn.eventbus.core.support.*;
import com.github.likavn.eventbus.core.support.spi.IJson;
import lombok.Data;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * json测试
 *
 * @author likavn
 * @date 2024/5/23
 */
public class JSONProviderTest {

    @Test
    public void toJSONString() {
        String val1="{\"age\":18,\"name\":\"likavn\"}";
        String val2="[{\"age\":20,\"name\":\"张三\"},{\"age\":18,\"name\":\"likavn\"}]";
        List<TestBody> bodys = new ArrayList<>(2);
        TestBody body = new TestBody();
        body.setName("张三");
        body.setAge(20);
        bodys.add(body);
        body = new TestBody();
        body.setName("likavn");
        body.setAge(18);
        bodys.add(body);
        IJson json = new Fast2jsonProvider();
        if (json.active()) {
            System.out.println("Fast2jsonProvider test");
            String str = json.toJsonString(body);
            System.out.println(str);
            Assertions.assertEquals(val1, str);
            str = json.toJsonString(bodys);
            System.out.println(str);
            Assertions.assertEquals(val2, str);
        }
         json = new FastjsonProvider();
        if (json.active()) {
            System.out.println("FastjsonProvider test");
            String str = json.toJsonString(body);
            System.out.println(str);
            Assertions.assertEquals(val1, str);
            str = json.toJsonString(bodys);
            System.out.println(str);
            Assertions.assertEquals(val2, str);
        }
        json = new JacksonProvider();
        if (json.active()) {
            System.out.println("JacksonProvider test");
            String str = json.toJsonString(body);
            System.out.println(str);
         //   Assertions.assertEquals(val1, str);
            str = json.toJsonString(bodys);
            System.out.println(str);
        //    Assertions.assertEquals(val2, str);
        }
         json = new GsonProvider();
        if (json.active()) {
            System.out.println("GsonProvider test");
            String str = json.toJsonString(body);
            System.out.println(str);
        //    Assertions.assertEquals(val1, str);
            str = json.toJsonString(bodys);
            System.out.println(str);
        //    Assertions.assertEquals(val2, str);
        }

    }

    @Data
    public static class TestBody {
        private String name;
        private Integer age;
        private BigDecimal amount;
    }


}
