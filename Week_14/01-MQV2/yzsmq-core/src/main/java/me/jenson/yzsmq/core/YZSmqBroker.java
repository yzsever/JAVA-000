package me.jenson.yzsmq.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class YZSmqBroker { // Broker+Connection

    public static final int CAPACITY = 10000;

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

}
