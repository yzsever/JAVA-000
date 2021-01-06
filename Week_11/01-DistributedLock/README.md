### 作业要求：（必做）基于 Redis 封装分布式数据操作：
1. 在 Java 中实现一个简单的分布式锁；
2. 在 Java 中实现一个分布式计数器，模拟减库存。

### 分布式锁
```
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
```


### 计数器
```
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
```
