package me.jenson.yzsmq.demo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.SneakyThrows;
import me.jenson.yzsmq.core.YZSConsumer;
import me.jenson.yzsmq.core.YZSProducer;
import me.jenson.yzsmq.core.YZSmqMessage;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class YZSmqDemo {

    @SneakyThrows
    public static void main(String[] args) {

        String topic = "yzs.test";
        YZSConsumer consumer = new YZSConsumer();
        Gson gson = new GsonBuilder().create();
        final boolean[] flag = new boolean[1];
        flag[0] = true;
        new Thread(() -> {
            while (flag[0]) {
                String messageStr = consumer.poll();
                if(null != messageStr) {
                    YZSmqMessage<Order> message = gson.fromJson(messageStr, YZSmqMessage.class);
                    System.out.println(message.getBody());
                    consumer.pollAck(topic);
                }
            }
            System.out.println("程序退出。");
        }).start();

        YZSProducer producer = new YZSProducer();
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
