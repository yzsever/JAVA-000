package gateway.outbound.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponseDecoder;
import io.netty.handler.codec.http.HttpVersion;
import org.apache.http.protocol.HTTP;

import java.net.URI;

public class NettyHttpClient {

    private String proxyServer;

    public NettyHttpClient(String proxyServer) {
        this.proxyServer = proxyServer;
    }

    Bootstrap b = new Bootstrap();
    //创建反应器线程组
    EventLoopGroup workerLoopGroup = new NioEventLoopGroup();

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
