## 作业5. 写一段代码，使用 HttpClient 或 OkHttp 访问 http://localhost:8801，代码提交到 Github。

### HttpClient
```java
package java0.nio01.client;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class HttpClient01 {

    public static void main(String[] args) {
        // 创建同步CloseableHttpClient对象
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        String api = "/test";
        String url = String.format("%s%s", "localhost:8808", api);
        // 创建HttpGet请求对象
        HttpGet httpGet = new HttpGet(url);
        CloseableHttpResponse response = null;
        try {
            // 调用execute方法执行请求
            response = httpClient.execute(httpGet);
            // 打印请求响应内容
            System.out.println(EntityUtils.toString(response.getEntity()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```

### OKHttp
```java
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
        String url = String.format("%s%s", "localhost:8808", api);
        // 创建Request对象
        Request request = new Request.Builder().url(url).get().build();
        // 将Request 对象封装为Call
        Call call = client.newCall(request);
        try {
            // 通过Call调用execute方法同步执行
            Response response = response = call.execute();
            // 打印请求响应内容
            System.out.println(response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```