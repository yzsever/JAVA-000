package me.jenson;

import me.jenson.service.Producer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ActiveMQTest {

    @Autowired
    private Producer producer;

    @Test
    public void sendSimpleQueueMessage() {
        this.producer.sendMsg("我是Queue生产者");
    }

    @Test
    public void sendSimpleTopicMessage() {
        this.producer.sendTopic("我是Topic生产者");
    }
}
