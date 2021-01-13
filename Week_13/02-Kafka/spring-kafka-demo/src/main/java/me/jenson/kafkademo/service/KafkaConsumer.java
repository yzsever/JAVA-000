package me.jenson.kafkademo.service;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class KafkaConsumer {

    @KafkaListener(topics = {"test32"})
    public void consume(ConsumerRecord<?, ?> record) {
        Optional<?> message = Optional.ofNullable(record.value());
        if (message.isPresent()) {
            System.out.println("KafkaConsumer receiver record = " + record);
            System.out.println("KafkaConsumer receiver message = " + message.get());
        }
    }
}
