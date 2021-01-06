package me.jenson.lock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class RedisLockDemo {

    @Autowired
    private RedisLock redisLock = new RedisLock();
    String key = "redis-lock";

    public void test() {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        int i = 10;
        while (i-- > 0) {
            executorService.execute(() -> lockAndUnlock());
        }
        executorService.shutdown();
    }

    public void lockAndUnlock() {
        String value = UUID.randomUUID().toString();
        final boolean islock = redisLock.lock_with_lua(key, value, 1);
        if (!islock) {
            System.out.println(Thread.currentThread()+":获取锁失败");
        } else {
            System.out.println(Thread.currentThread()+":获取锁成功");
            redisLock.unlock(jedis, key, value);
            System.out.println(Thread.currentThread()+":释放锁成功");
        }
    }
}
