package me.jenson.kafkademo;

import me.jenson.kafkademo.service.KafkaProducer;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
class KafkaDemoApplicationTests {

    @Autowired
    private KafkaProducer kafkaProducer;

    @Test
    void contextLoads() {
        kafkaProducer.produce();
    }
}
