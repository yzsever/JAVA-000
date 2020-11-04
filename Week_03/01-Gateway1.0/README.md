### 作业1. *周四作业(必做):整合你上次作业的 httpclient/okhttp;
>（可选)压测：调整NettyServer和HttpClient的参数

作业一只是将上次的作业整合到项目中，所以比较简单。

#### httpclient实现
HttpInboundHandler修改内容：
```
...
    private HttpClientOutboundHandler handler;
    
    public HttpInboundHandler(String proxyServer) {
        this.proxyServer = proxyServer;
        // 1、自己实现的同步的httpClient的方案
        handler = new HttpClientOutboundHandler(this.proxyServer);
    }
```

HttpClientOutboundHandler主要内容
```
public class HttpClientOutboundHandler {

    private CloseableHttpClient httpClient;
    private String backendUrl;

    public HttpClientOutboundHandler(String backendUrl) {
        this.backendUrl = backendUrl.endsWith("/")?backendUrl.substring(0,backendUrl.length()-1):backendUrl;
        // 创建同步CloseableHttpClient对象
        httpClient = HttpClientBuilder.create().build();
    }

    public void handle(final FullHttpRequest fullRequest, final ChannelHandlerContext ctx) {
        final String url = this.backendUrl + fullRequest.uri();
        fetchGet(fullRequest, ctx, url);
    }

    private void fetchGet(final FullHttpRequest inbound, final ChannelHandlerContext ctx, final String url) {
        final HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader(HTTP.CONN_DIRECTIVE, HTTP.CONN_KEEP_ALIVE);
        // 创建HttpGet请求对象
        CloseableHttpResponse endpointResponse = null;
        try {
            // 调用execute方法执行请求
            endpointResponse = httpClient.execute(httpGet);
            // 处理请求响应内容
            handleResponse(inbound, ctx, endpointResponse);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleResponse(final FullHttpRequest fullRequest, final ChannelHandlerContext ctx, final HttpResponse endpointResponse) throws Exception {
        FullHttpResponse response = null;
        try {
            byte[] body = EntityUtils.toByteArray(endpointResponse.getEntity());
            System.out.println("HttpClient:"+new String(body));
            response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(body));
            response.headers().set("Content-Type", "application/json");
            response.headers().setInt("Content-Length", Integer.parseInt(endpointResponse.getFirstHeader("Content-Length").getValue()));

        } catch (Exception e) {
            e.printStackTrace();
            response = new DefaultFullHttpResponse(HTTP_1_1, NO_CONTENT);
            exceptionCaught(ctx, e);
        } finally {
            if (fullRequest != null) {
                if (!HttpUtil.isKeepAlive(fullRequest)) {
                    ctx.write(response).addListener(ChannelFutureListener.CLOSE);
                } else {
                    //response.headers().set(CONNECTION, KEEP_ALIVE);
                    ctx.write(response);
                }
            }
            ctx.flush();
            //ctx.close();
        }

    }

    ...

}
```

#### okhttp实现
HttpInboundHandler修改内容：
```
...
    private OkHttpOutboundHandler handler;
    
    public HttpInboundHandler(String proxyServer) {
        this.proxyServer = proxyServer;
        // 2、自己实现的同步的okHttp的方案
        handler = new OkHttpOutboundHandler(this.proxyServer);
    }
...
```

OkHttpOutboundHandler主要内容
```
public class OkHttpOutboundHandler {

    private OkHttpClient okHttpClient;
    private Call call;
    private String backendUrl;

    public OkHttpOutboundHandler(String backendUrl) {
        this.backendUrl = backendUrl.endsWith("/")?backendUrl.substring(0,backendUrl.length()-1):backendUrl;
        // 创建OkHttpClient对象
        okHttpClient = new OkHttpClient();
    }

    public void handle(final FullHttpRequest fullRequest, final ChannelHandlerContext ctx) {
        final String url = this.backendUrl + fullRequest.uri();
        fetchGet(fullRequest, ctx, url);
    }

    private void fetchGet(final FullHttpRequest inbound, final ChannelHandlerContext ctx, final String url) {
        // 创建Request对象
        Request request = new Request.Builder().url(url).get().build();
        // 将Request 对象封装为Call
        call = okHttpClient.newCall(request);
        Response endpointResponse = null;
        try {
            // 通过Call调用execute方法同步执行
            endpointResponse = call.execute();
            // 处理请求响应内容
            handleResponse(inbound, ctx, endpointResponse);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleResponse(final FullHttpRequest fullRequest, final ChannelHandlerContext ctx, final Response endpointResponse) throws Exception {
        FullHttpResponse response = null;
        try {
            byte[] body = endpointResponse.body().string().getBytes();
            System.out.println("OKHttp:"+new String(body));
            response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(body));
            response.headers().set("Content-Type", "application/json");
            response.headers().setInt("Content-Length", (int) endpointResponse.body().contentLength());
        } catch (Exception e) {
            e.printStackTrace();
            response = new DefaultFullHttpResponse(HTTP_1_1, NO_CONTENT);
            exceptionCaught(ctx, e);
        } finally {
            if (fullRequest != null) {
                if (!HttpUtil.isKeepAlive(fullRequest)) {
                    ctx.write(response).addListener(ChannelFutureListener.CLOSE);
                } else {
                    ctx.write(response);
                }
            }
            ctx.flush();
        }

    }
    ...
}
```

