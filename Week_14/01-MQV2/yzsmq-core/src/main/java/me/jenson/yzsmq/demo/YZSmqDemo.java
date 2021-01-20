package me.jenson.yzsmq.demo;

import me.jenson.yzsmq.core.YZSmqBroker;
import me.jenson.yzsmq.core.YZSmqConsumer;
import me.jenson.yzsmq.core.YZSmqMessage;
import me.jenson.yzsmq.core.YZSmqProducer;

import lombok.SneakyThrows;

public class YZSmqDemo {

    @SneakyThrows
    public static void main(String[] args) {

        String topic = "yzs.test";
        YZSmqBroker broker = new YZSmqBroker();
        broker.createTopic(topic);

        YZSmqConsumer consumer = broker.createConsumer();
        consumer.subscribe(topic);
        final boolean[] flag = new boolean[1];
        flag[0] = true;
        new Thread(() -> {
            while (flag[0]) {
                YZSmqMessage<Order> message = consumer.poll(consumer.getUuid());
                if(null != message) {
                    System.out.println(message.getBody());
                    consumer.ackPoll(consumer.getUuid());
                }
            }
            System.out.println("程序退出。");
        }).start();

        YZSmqProducer producer = broker.createProducer();
        for (int i = 0; i < 1000; i++) {
            Order order = new Order(1000L + i, System.currentTimeMillis(), "USD2CNY", 6.51d);
            producer.send(topic, new YZSmqMessage(null, order));
            producer.sendAck(topic);
        }
        Thread.sleep(500);
        System.out.println("点击任何键，发送一条消息；点击q或e，退出程序。");
        while (true) {
            char c = (char) System.in.read();
            if(c > 20) {
                System.out.println(c);
                producer.send(topic, new YZSmqMessage(null, new Order(100000L + c, System.currentTimeMillis(), "USD2CNY", 6.52d)));
                producer.sendAck(topic);
            }

            if( c == 'q' || c == 'e') break;
        }

        flag[0] = false;

    }
}
