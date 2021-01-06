package me.jenson;

import me.jenson.counter.RedisCounterDemo;
import me.jenson.lock.RedisLockDemo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

@SpringBootApplication(scanBasePackages = "me.jenson")
public class RedisApplication {

	@Autowired
	public RedisCounterDemo redisCounterDemo;

	public static void main(final String[] args) {
		SpringApplication.run(RedisApplication.class, args);
	}

	@PostConstruct
	public void executeDemo() {
		RedisLockDemo redisLockDemo = new RedisLockDemo();
		redisLockDemo.test();
		redisCounterDemo.test();
	}

}
