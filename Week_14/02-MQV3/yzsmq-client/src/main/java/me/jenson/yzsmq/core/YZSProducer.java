package me.jenson.yzsmq.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.jenson.yzsmq.demo.Order;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

public class YZSProducer {

    private Gson gson = new GsonBuilder().create();

    public void send(String topic, YZSmqMessage<Order> message){
        CloseableHttpClient client = HttpClients.createDefault();
        String api = "/poll";
        String url = String.format("%s%s", "localhost:8066", api);
        HttpPost httpPost = new HttpPost(url);

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("topic", topic));
        params.add(new BasicNameValuePair("message", gson.toJson(message)));
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(params));
            CloseableHttpResponse response = client.execute(httpPost);
            client.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendAck(String topic){
        CloseableHttpClient client = HttpClients.createDefault();
        String api = "/send_ack";
        String url = String.format("%s%s", "localhost:8066", api);
        HttpPost httpPost = new HttpPost(url);

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("topic", topic));
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(params));
            CloseableHttpResponse response = client.execute(httpPost);
            client.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
