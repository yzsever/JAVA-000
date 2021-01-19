package me.jenson.yzsmq.core;

public class YZSmqConsumer<T> {

    private final YZSmqBroker broker;

    private YZSmq YZSmq;

    public YZSmqConsumer(YZSmqBroker broker) {
        this.broker = broker;
    }

    public void subscribe(String topic) {
        this.YZSmq = this.broker.findYZSmq(topic);
        if (null == YZSmq) throw new RuntimeException("Topic[" + topic + "] doesn't exist.");
    }

    public YZSmqMessage<T> poll(long timeout) {
        return YZSmq.poll(timeout);
    }

}
