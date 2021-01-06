package me.jenson.counter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisCounter {

    private Long count;
    private String key = "inventory-counter";

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public void setCount(Long count) {
        this.count = count;
        redisTemplate.delete(key);
    }

    public boolean reduce(){
        long leftCount = redisTemplate.opsForValue().increment(key, 1);
        if(leftCount > count){
            return false;
        }
        return true;
    }
}
