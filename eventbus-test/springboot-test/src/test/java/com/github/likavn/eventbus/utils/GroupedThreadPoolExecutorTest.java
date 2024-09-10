package com.github.likavn.eventbus.utils;

import com.github.likavn.eventbus.core.utils.GroupedThreadPoolExecutor;
import com.github.likavn.eventbus.core.utils.NamedThreadFactory;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GroupedThreadPoolExecutorTest {

    @Test
    public void testTask() throws InterruptedException {
        GroupedThreadPoolExecutor executor = new GroupedThreadPoolExecutor(
                1, 1000 * 5, new NamedThreadFactory("test-thread-pool-"));
        AtomicInteger time = new AtomicInteger(0);
        AtomicInteger executeNum = new AtomicInteger(0);
        AtomicInteger index = new AtomicInteger(0);

        int pollNum = 100;
        int streamSize = 500;
        int realNum = pollNum * streamSize;
        CountDownLatch latch = new CountDownLatch(Math.toIntExact(realNum));
        Stream.of(new Byte[pollNum]).parallel().forEach(t -> {
            index.incrementAndGet();
            new Thread(() -> Stream.of(new Byte[streamSize]).parallel().forEach(t1 -> {
             //   int timeNum = time.incrementAndGet();
                GroupedThreadPoolExecutor.GTask task = new GroupedThreadPoolExecutor.GTask();
                task.setName("task-" + index.get());
                task.setConcurrency(2);
                //     System.out.println("task-" + index + "->" + timeNum + "，threadName->" + Thread.currentThread().getName());
                task.target(() -> {
                //    System.out.println("execute name->" + task.getName() + "   " + Thread.currentThread().getName());
                    executeNum.incrementAndGet();
                    latch.countDown();
                });
                executor.execute(task);
            })).start();
        });
        latch.await();
        if (executeNum.get() == realNum) {
            System.out.println("成功...  " + executeNum.get());
        } else {
            System.out.println("失败...  " + executeNum.get() + ",相差：" + (realNum - executeNum.get()));
        }
        assertEquals(realNum, executeNum.get());
    }

}
