package me.jenson.lock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class RedisLock {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    String lockLUAScripts = "if redis.call('setnx', KEYS[1], ARGV[1]) == 1 then " +
            "redis.call('expire',KEYS[1], ARGV[2]) return 1 else return 0 end";
    String unLockLUAScripts = "if redis.call('get',KEYS[1]) == ARGV[1] then " +
            "return redis.call('del',KEYS[1]) else return 0 end";

    /**
     * 使用Lua脚本，脚本中使用setnex+expire命令进行加锁操作
     */
    public boolean lock_with_lua(String key, String UUID) {
        DefaultRedisScript<Long> lockScript = new DefaultRedisScript<>(lockLUAScripts, Long.class);
        List<String> args = new ArrayList<>();
        args.add(UUID);
        args.add(String.valueOf(1));
        Long result = redisTemplate.execute(lockScript, Collections.singletonList(key), UUID, String.valueOf(1));
        //判断是否成功
        return result.equals(1L);
    }

    /**
     * 使用Lua脚本进行解锁操纵，解锁的时候验证value值
     */
    public boolean unlock(String key, String value) {
        DefaultRedisScript<Long> lockScript = new DefaultRedisScript<>(unLockLUAScripts, Long.class);
        Long result = redisTemplate.execute(lockScript, Collections.singletonList(key), value);
        //判断是否成功
        return result.equals(1L);
    }
}