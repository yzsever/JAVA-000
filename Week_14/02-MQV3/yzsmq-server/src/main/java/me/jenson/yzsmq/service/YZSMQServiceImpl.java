package me.jenson.yzsmq.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.jenson.yzsmq.core.YZSmqBroker;
import me.jenson.yzsmq.core.YZSmqConsumer;
import me.jenson.yzsmq.core.YZSmqMessage;
import me.jenson.yzsmq.core.YZSmqProducer;
import me.jenson.yzsmq.demo.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class YZSMQServiceImpl implements YZSMQService{
    @Autowired
    private YZSmqBroker yzsmqBroker;

    private Gson gson = new GsonBuilder().create();

    @Override
    public String poll(String topic, String uuid) {
        YZSmqConsumer consumer = yzsmqBroker.createConsumer(uuid);
        consumer.subscribe(topic);
        YZSmqMessage<Order> message = consumer.poll(consumer.getUuid());
        if(null != message) {
            System.out.println(message.getBody());
            return gson.toJson(message);
        }
        return null;
    }

    @Override
    public void send(String topic, String message) {
        YZSmqProducer producer = yzsmqBroker.createProducer();
        producer.send(topic, gson.fromJson(message, YZSmqMessage.class));
    }

    @Override
    public void pollAck(String topic, String uuid) {
        YZSmqConsumer consumer = yzsmqBroker.createConsumer(uuid);
        consumer.subscribe(topic);
        consumer.ackPoll(consumer.getUuid());
    }

    @Override
    public void sendAck(String topic) {
        YZSmqProducer producer = yzsmqBroker.createProducer();
        producer.ack(topic);
    }
}
