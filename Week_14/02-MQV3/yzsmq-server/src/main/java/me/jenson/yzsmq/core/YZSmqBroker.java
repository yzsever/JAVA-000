package me.jenson.yzsmq.core;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public final class YZSmqBroker { // Broker+Connection

    public static final int CAPACITY = 100000;

    private final Map<String, YZSmq> yzsmqMap = new ConcurrentHashMap<>(64);

    public void createTopic(String name){
        yzsmqMap.putIfAbsent(name, new YZSmq(name,CAPACITY));
    }

    public YZSmq findYZSmq(String topic) {
        return this.yzsmqMap.get(topic);
    }

    public YZSmqProducer createProducer() {
        return new YZSmqProducer(this);
    }

    public YZSmqConsumer createConsumer() {
        return new YZSmqConsumer(this);
    }

    public YZSmqConsumer createConsumer(String uuid) {
        return new YZSmqConsumer(this, uuid);
    }
}
