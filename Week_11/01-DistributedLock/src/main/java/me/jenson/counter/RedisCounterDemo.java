package me.jenson.counter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
public class RedisCounterDemo {

    @Autowired
    public RedisCounter redisCounter;

    @GetMapping("/counter")
    public void test() {
        redisCounter.setCount(5L);
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        int i = 10;
        while (i-- > 0) {
            executorService.execute(() -> buy());
        }
        executorService.shutdown();
    }

    public void buy() {
        if (redisCounter.reduce()) {
            System.out.println(Thread.currentThread() + " 购买===成功");
        } else {
            System.out.println(Thread.currentThread() + " 购买===失败");
        }
    }
}
