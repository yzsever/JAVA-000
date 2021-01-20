package me.jenson.yzsmq.core;

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
import java.util.UUID;

public class YZSConsumer {

    String uuid = UUID.randomUUID().toString();

    public String poll(){
        // 创建同步CloseableHttpClient对象
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        String api = "/poll";
        String url = String.format("%s%s", "localhost:8066", api);
        // 创建HttpGet请求对象
        HttpGet httpGet = new HttpGet(url);
        CloseableHttpResponse response = null;
        try {
            // 调用execute方法执行请求
            response = httpClient.execute(httpGet);
            // 打印请求响应内容
            System.out.println(EntityUtils.toString(response.getEntity()));
            return response.getEntity().getContent().toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void pollAck(String topic){
        CloseableHttpClient client = HttpClients.createDefault();
        String api = "/poll";
        String url = String.format("%s%s", "localhost:8066", api);
        HttpPost httpPost = new HttpPost(url);

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("topic", topic));
        params.add(new BasicNameValuePair("uuid", uuid));
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(params));
            CloseableHttpResponse response = client.execute(httpPost);
            client.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
