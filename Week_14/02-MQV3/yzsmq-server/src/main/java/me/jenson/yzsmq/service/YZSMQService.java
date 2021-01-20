package me.jenson.yzsmq.service;

public interface YZSMQService {
    String poll(String topic, String uuid);

    void send(String topic, String message);

    void pollAck(String topic, String uuid);

    void sendAck(String topic);
}
