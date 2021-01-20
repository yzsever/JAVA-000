package me.jenson.yzsmq.core;

import lombok.SneakyThrows;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public final class YZSmq {

    private String topic;

    private int capacity;

    private YZSmqQueue queue;

    private Map<String, Integer> consumerOffsetMap = new HashMap<>();

    public YZSmq(String topic, int capacity) {
        this.topic = topic;
        this.capacity = capacity;
        this.queue = new YZSmqQueue(capacity);
    }

    public boolean send(YZSmqMessage message) {
        return queue.offer(message);
    }

    public YZSmqMessage poll(String uuid) {
        Integer offset = consumerOffsetMap.get(uuid);
        if(offset == null){
            offset = 0;
        }
        return queue.poll(offset);
    }

    @SneakyThrows
    public YZSmqMessage poll(long timeout) {
        return queue.poll(timeout, TimeUnit.MILLISECONDS);
    }

    public void ackPoll(String uuid) {
        Integer offset = consumerOffsetMap.get(uuid);
        if(offset == null){
            offset = 0;
        }
        consumerOffsetMap.put(uuid, ++offset);
    }

    public void ackSend() {
        queue.updatePollOffset();
    }
}
