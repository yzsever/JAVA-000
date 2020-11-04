### 作业2. (可选):使用netty实现后端http访问(代替上一步骤);
> 压测：调整NettyServer和NettyClient的参数

由于对Netty不是很熟悉，所以使用netty实现后端花了很多的时间，也经历很多的错误思路。这里最终通过和同学的交流学习最终实现了一个简单的版本。流程如下：

#### 1.NettyServer收到外部请求后，在InboundHandler中，创建一个NettyClient
```java
public class HttpInboundHandler extends ChannelInboundHandlerAdapter {
    private final String proxyServer;
    private NettyHttpClient handler;

    public HttpInboundHandler(String proxyServer) {
        this.proxyServer = proxyServer;
        handler = new NettyHttpClient(proxyServer);
    }
...

}
```

#### 2.ChannelRead处理业务时，调用NettyClient的handle函数，并将此时外部Client和NettyServer的通信上下文对象(serverCtx)发送过去
```java
public class HttpInboundHandler extends ChannelInboundHandlerAdapter {
...
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            FullHttpRequest fullRequest = (FullHttpRequest) msg;
            handler.handle(fullRequest, ctx);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

}
```

#### 3.NettyClient收到请求后，创建新的反应器线程组，并创建后端服务调用请求，将请求写入channel
```java
public class NettyHttpClient {

...

    public void runClient(final FullHttpRequest fullRequest, final ChannelHandlerContext ctx) {
        try {
            //1 设置反应器 线程组
            b.group(workerLoopGroup);
            //2 设置nio类型的通道
            b.channel(NioSocketChannel.class);
            //3 设置监听端口
            URI uri = new URI(proxyServer);
            b.remoteAddress(uri.getHost(), uri.getPort());
            //4 设置通道的参数
            b.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
            b.option(ChannelOption.SO_KEEPALIVE, true);
            //5 装配子通道流水线
            b.handler(new ChannelInitializer<SocketChannel>() {
                //有连接到达时会创建一个通道
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    // 客户端接收到的是httpResponse响应，所以要使用HttpResponseDecoder进行解码
                    ch.pipeline().addLast(new HttpResponseDecoder());
                    ch.pipeline().addLast(new HttpObjectAggregator(1024 * 1024));
                    // 客户端发送的是httprequest，所以要使用HttpRequestEncoder进行编码
                    ch.pipeline().addLast(new HttpRequestEncoder());
                    ch.pipeline().addLast(new NettyHttpClientOutboundHandler(ctx));
                }
            });

            ChannelFuture f = b.connect();
            f.addListener((ChannelFuture futureListener) ->
            {
                if (futureListener.isSuccess()) {
                    System.out.println("EchoClient客户端连接成功!");
                } else {
                    System.out.println("EchoClient客户端连接失败!");
                }
            });
            // 阻塞,直到连接成功
            f.sync();
            // 向后端服务器发送请求
            FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_0, HttpMethod.GET, proxyServer + fullRequest.uri());
            request.headers().add(HTTP.CONN_DIRECTIVE, HTTP.CONN_KEEP_ALIVE);
            request.headers().add(HTTP.TARGET_HOST, uri.getHost() + ":" + uri.getPort());
            f.channel().writeAndFlush(request);

            // 7 等待通道关闭的异步任务结束
            // 服务监听通道会一直等待通道关闭的异步任务结束
            ChannelFuture closeFuture = f.channel().closeFuture();
            closeFuture.sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 从容关闭EventLoopGroup，
            // 释放掉所有资源，包括创建的线程
            workerLoopGroup.shutdownGracefully();
        }
    }

    public void handle(final FullHttpRequest fullRequest, final ChannelHandlerContext ctx) {
        this.runClient(fullRequest, ctx);
    }
}
```

#### 4.NettyClient的handler处理后端服务调用结果，通过serverCtx发送给NettyServer
```java
public class NettyHttpClientOutboundHandler extends ChannelInboundHandlerAdapter {
...
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            // 将后端服务的结果返回给NettyServer
            FullHttpResponse endpointResponse = (FullHttpResponse) msg;
            ByteBuf buf = endpointResponse.content();
            byte[] responseBuf = new byte[buf.readableBytes()];
            buf.readBytes(responseBuf);
            handleResponse(null, serverCtx, responseBuf);
            ctx.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }
...
}
```

#### 5.NettyServer将通过ServerCtx将结果返回给外部Client



