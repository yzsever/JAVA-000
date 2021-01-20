package me.jenson.yzsmq.core;

import lombok.Data;

import java.util.UUID;

@Data
public class YZSmqConsumer<T> {

    private final YZSmqBroker broker;

    private YZSmq YZSmq;

    private String uuid;

    public YZSmqConsumer(YZSmqBroker broker) {
        this.broker = broker;
        this.uuid = UUID.randomUUID().toString();
    }

    public YZSmqConsumer(YZSmqBroker broker, String uuid) {
        this.broker = broker;
        this.uuid = uuid;
    }

    public void subscribe(String topic) {
        this.YZSmq = this.broker.findYZSmq(topic);
        if (null == YZSmq) throw new RuntimeException("Topic[" + topic + "] doesn't exist.");
    }

    public YZSmqMessage<T> poll(String uuid) {
        return YZSmq.poll(uuid);
    }

    public void poll(long timeout) {
        YZSmq.poll(timeout);
    }

    public void ackPoll(String uuid){
        YZSmq.ackPoll(uuid);
    }

}
