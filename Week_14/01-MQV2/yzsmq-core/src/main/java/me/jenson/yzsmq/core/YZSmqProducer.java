package me.jenson.yzsmq.core;

public class YZSmqProducer {

    private YZSmqBroker broker;

    public YZSmqProducer(YZSmqBroker broker) {
        this.broker = broker;
    }

    public boolean send(String topic, YZSmqMessage message) {
        YZSmq YZSmq = this.broker.findYZSmq(topic);
        if (null == YZSmq) throw new RuntimeException("Topic[" + topic + "] doesn't exist.");
        return YZSmq.send(message);
    }

    public void ack(String topic){
        YZSmq YZSmq = this.broker.findYZSmq(topic);
        YZSmq.ackSend();
    }
}
