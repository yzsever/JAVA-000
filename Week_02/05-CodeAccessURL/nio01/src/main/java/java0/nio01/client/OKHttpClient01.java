package java0.nio01.client;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class OKHttpClient01 {

    public static void main(String[] args) {
        // 创建OkHttpClient对象
        OkHttpClient client = new OkHttpClient();
        String api = "/test";
        String url = String.format("%s%s", "http://localhost:8808", api);
        // 创建Request对象
        Request request = new Request.Builder().url(url).get().build();
        // 将Request 对象封装为Call
        Call call = client.newCall(request);
        try {
            // 通过Call调用execute方法同步执行
            Response response = call.execute();
            // 打印请求响应内容
            System.out.println(response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
