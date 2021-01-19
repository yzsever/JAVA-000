package me.jenson.yzsmq.core;

import lombok.SneakyThrows;

import java.util.concurrent.TimeUnit;

public final class YZSmq {

    public YZSmq(String topic, int capacity) {
        this.topic = topic;
        this.capacity = capacity;
        this.queue = new YZSmqQueue(capacity);
    }

    private String topic;

    private int capacity;

    private YZSmqQueue queue;

    public boolean send(YZSmqMessage message) {
        return queue.offer(message);
    }

    public YZSmqMessage poll() {
        return queue.poll();
    }

    @SneakyThrows
    public YZSmqMessage poll(long timeout) {
        return queue.poll(timeout, TimeUnit.MILLISECONDS);
    }

    public void ackSend() {
        queue.updatePollOffset();
    }
}
