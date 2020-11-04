### 作业3.（必做）:实现一个request的过滤器filter。
1. filter里把我们请求的http头拿到，然后在里面添加一个新的key-value，key是nio，value是自己的名字拼音。
2. 实际请求后端服务的时候，将原请求所有的头拿出来，在实际调用后端服务之前全部添加到对后端的请求头里。（我们去调用后端服务的时候，那个请求里面就比网关接入请求多一个我们自定义的头。这样就相当于我们的程序在网关的filter这边做了一个加强）

---
实现过滤器注意有以下几个步骤
### 1.新建Http请求头过滤器
```java
public class HttpReqeustHeaderFilter implements HttpRequestFilter{
    public void filter(FullHttpRequest fullRequest, ChannelHandlerContext ctx) {
        HttpHeaders headers = fullRequest.headers();
        // 在fullRequest里面添加一个新的key-value，key是nio，value是自己的名字拼音。
        headers.add("nio", "JensonYao");
    }
}
```

### 2.过滤器的应用在原HttpInboundHandle处理之前
- 这里可以在原HttpInboundHandle创建一个过滤器实例，调用filter方法
- 实现的是创建一个新的入站过滤器处理器，在ChannelPipeline中添加在原HttpInboundHandle之前
```java
public class HttpRequesFilterInBoundHandler extends ChannelInboundHandlerAdapter {

    private List<HttpRequestFilter> filters;

    public HttpRequesFilterInBoundHandler() {
        filters = new ArrayList<HttpRequestFilter>();
        filters.add(new HttpReqeustHeaderFilter());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            FullHttpRequest fullRequest = (FullHttpRequest) msg;
            // 执行过滤操作
            for(HttpRequestFilter filter : filters){
                filter.filter(fullRequest, ctx);
            }
            // 父类的channelRead()方法会自动调用下一个inBoundHandler的channelRead()方法,
            // 并且会把当前inBoundHandler入站处理器中处理完毕的对象传递到下一个inBoundHandler入站处理器
            super.channelRead(ctx, fullRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
...
}
```

### 3.实际请求后端服务的时候，将原请求所有的头拿出来，在实际调用后端服务之前全部添加到对后端的请求头里。
```java

public class NettyHttpClient {
...
    public void runClient(final FullHttpRequest fullRequest, final ChannelHandlerContext ctx) {
...
            // 向后端服务器发送请求
            FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_0, HttpMethod.GET, proxyServer + fullRequest.uri());
            // 将原请求所有的头拿出来，在实际调用后端服务之前全部添加到对后端的请求头里
            for (Map.Entry<String, String> entry : fullRequest.headers()) {
                request.headers().add(entry.getKey(), entry.getValue());
            }
            for (Map.Entry<String, String> entry : request.headers()) {
                System.out.println("After Filter Header:" + entry.getKey() + ":" + entry.getValue());
            }
            f.channel().writeAndFlush(request);

            // 7 等待通道关闭的异步任务结束
            // 服务监听通道会一直等待通道关闭的异步任务结束
...
        }
    }
...
}

```



