### 作业4. (选做):实现路由
把现在只代理的一个后端服务，变成了可以代理多个后端服务。我们通过最简的路由算法，比如说随机的对后端的两个服务取一个，然后调用这个服务返回数据。

---
实现路由功能的步骤如下：

#### 1.创建一个后端路由器，通过route方法可以随机获得一个后端服务器地址
```java
public class HttpEndpointRandomRouter implements HttpEndpointRouter{
    @Override
    public String route(List<String> endpoints) {
        Random random = new Random();
        int randomPos = random.nextInt(endpoints.size());
        // 随机获得一个后端服务器地址
        String endpoint = endpoints.get(randomPos);
        System.out.println("Select endpoint: "+endpoint);
        return endpoint;
    }
}
```

#### 2.创建Endpoint工厂类，配置可用后端，并可以获得后端服务
```java
public class EndpointFactory {
    // 通过单例模式创建工厂类
    private final static EndpointFactory INSTACNE = new EndpointFactory();
    private final HttpEndpointRandomRouter router = new HttpEndpointRandomRouter();
    private List<String> endpoints = new ArrayList<>();

    // 配置可用后端服务
    public EndpointFactory(){
        endpoints.add("http://127.0.0.1:8807");
        endpoints.add("http://127.0.0.1:8808");
        endpoints.add("http://127.0.0.1:8809");
    }

    // 调用随机路由获取一个后端服务
    public String getEndpoint(){
        return router.route(endpoints);
    }

    public static EndpointFactory getInstance(){
        return INSTACNE;
    }
}
```

#### 3.NettyClient在处理请求时，通过Endpoint工厂类获取调用后端服务地址
```java
public class NettyHttpClient {
...
    public void runClient(final FullHttpRequest fullRequest, final ChannelHandlerContext ctx) {
...
            //3 设置监听端口
            // 通过路由获取后端服务地址
            String proxyServer = EndpointFactory.getInstance().getEndpoint();
            URI uri = new URI(proxyServer);
            b.remoteAddress(uri.getHost(), uri.getPort());
            //4 设置通道的参数
...
    }
...
}

```




