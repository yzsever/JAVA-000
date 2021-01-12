package me.jenson.service;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class Consumer {

    @JmsListener(destination = "test.queue", containerFactory = "queueListenerFactory")
    public void queueConsume(String text) {
        System.out.println("接收到queue的消息 : " + text);
    }

    @JmsListener(destination = "test.topic", containerFactory = "topicListenerFactory")
    public void topicConsume1(String text) {
        System.out.println("消费者1 接收到queue的消息 : " + text);
    }

    @JmsListener(destination = "test.topic", containerFactory = "topicListenerFactory")
    public void topicConsume2(String text) {
        System.out.println("消费者2 接收到queue的消息 : " + text);
    }
}