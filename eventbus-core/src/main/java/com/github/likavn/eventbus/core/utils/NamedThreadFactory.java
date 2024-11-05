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
package com.github.likavn.eventbus.core.utils;


import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadFactory;

/**
 * 带有前缀名称的线程工厂
 *
 * @author likavn
 * @date 2024/01/01
 */
@Getter
public class NamedThreadFactory implements ThreadFactory {

    /**
     * 线程名前缀
     */
    private final String prefix;

    /**
     * 线程编号
     */
    private volatile int threadNumber = 0;

    /**
     * 回收的线程编号列表
     */
    private final List<Integer> returnThreadNumbers = new ArrayList<>(0);

    /**
     * 创建线程工厂
     *
     * @param prefix 线程名前缀
     */
    public NamedThreadFactory(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public Thread newThread(Runnable r) {
        return new Thread(null, r, prefix + increment());
    }

    /**
     * 增加一个可用资源，采用原子操作保证线程安全。
     */
    public synchronized int increment() {
        if (!returnThreadNumbers.isEmpty()) {
            return returnThreadNumbers.remove(returnThreadNumbers.size() - 1);
        }
        return ++threadNumber;
    }

    /**
     * 返回一个线程编号
     */
    public synchronized void decrement(Integer theadNumber) {
        returnThreadNumbers.add(theadNumber);
        returnThreadNumbers.sort(Integer::compareTo);
    }

    /**
     * 清空线程编号
     */
    public synchronized void clear() {
        threadNumber = 0;
        returnThreadNumbers.clear();
    }
}
