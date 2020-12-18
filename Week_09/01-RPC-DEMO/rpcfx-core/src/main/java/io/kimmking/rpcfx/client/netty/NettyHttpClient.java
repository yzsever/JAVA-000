package io.kimmking.rpcfx.client.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import java.net.URI;
import java.util.concurrent.TimeUnit;

public class NettyHttpClient {

    Bootstrap b = new Bootstrap();
    //创建反应器线程组
    EventLoopGroup workerLoopGroup = new NioEventLoopGroup();

    public String runClient(final String requestStr, final String url) {
        String response = "";
        try {
            //1 设置反应器 线程组
            b.group(workerLoopGroup);
            //2 设置nio类型的通道
            b.channel(NioSocketChannel.class);
            //3 设置监听端口
            URI uri = new URI(url);
            b.remoteAddress(uri.getHost(), uri.getPort());
            //4 设置通道的参数
            b.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
            b.option(ChannelOption.SO_KEEPALIVE, true);
            NettyHttpClientOutboundHandler nettyHttpClientOutboundHandler = new NettyHttpClientOutboundHandler();
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
                    ch.pipeline().addLast(nettyHttpClientOutboundHandler);
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
            FullHttpRequest request = new DefaultFullHttpRequest(
                    HttpVersion.HTTP_1_1, HttpMethod.POST, uri.toASCIIString(), Unpooled.wrappedBuffer(requestStr.getBytes(CharsetUtil.UTF_8))
            );
            request.headers()
                    .set(HttpHeaderNames.HOST, uri.getHost())
                    .set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE)
                    .set(HttpHeaderNames.CONTENT_LENGTH, request.content().readableBytes())
                    .set(HttpHeaderNames.CONTENT_TYPE, "application/json");
            f.channel().writeAndFlush(request);

            // 7 等待通道关闭的异步任务结束
            // 服务监听通道会一直等待通道关闭的异步任务结束
            ChannelFuture closeFuture = f.channel().closeFuture();
            closeFuture.sync();
            response = nettyHttpClientOutboundHandler.getResponse(3, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 从容关闭EventLoopGroup，
            // 释放掉所有资源，包括创建的线程
            workerLoopGroup.shutdownGracefully();
        }
        return response;
    }

    public String handle(final String requestStr, final String url) {
        return this.runClient(requestStr, url);
    }


}
