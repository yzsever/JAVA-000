package me.jenson.kafkademo.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.jenson.kafkademo.entity.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

@Component
public class KafkaProducer {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private Gson gson = new GsonBuilder().create();

    public void produce() {
        Message message = new Message();
        message.setId(System.currentTimeMillis());
        message.setMessage(UUID.randomUUID().toString());
        message.setSendTime(new Date());
        System.out.println("KafkaProducer message = " + gson.toJson(message));
        kafkaTemplate.send("test32", gson.toJson(message));
    }
}
