package me.jenson.yzsmq.core;

import java.util.concurrent.TimeUnit;

public class YZSmqQueue {

    private YZSmqMessage[] queue;
    private final int maxCapacity = 100000;
    private int offerOffset = 0;
    private int pollOffset = 0;
    private int capacity;

    public YZSmqQueue(int capacity) {
        this.capacity = capacity > maxCapacity ? maxCapacity : capacity;
        queue = new YZSmqMessage[this.capacity];
    }

    public YZSmqQueue() {
        this.capacity = maxCapacity;
        queue = new YZSmqMessage[maxCapacity];
    }

    public boolean offer(YZSmqMessage message) {
        if(offerOffset >= capacity){
            return false;
        }
        queue[offerOffset++] = message;
        return true;
    }

    public YZSmqMessage poll(int offset) {
        return queue[offset];
    }

    public YZSmqMessage poll(long timeout, TimeUnit milliseconds) {
        return null;
    }

    public void updatePollOffset(){
        this.pollOffset = offerOffset - 1;
    }
}
