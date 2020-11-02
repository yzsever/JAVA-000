package io.github.kimmking.gateway.outbound.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponseDecoder;

import java.util.Scanner;

public class NettyHttpClient {

    private int serverPort;
    private String serverIp;
    Bootstrap b = new Bootstrap();

    public NettyHttpClient(String ip, int port) {
        this.serverPort = port;
        this.serverIp = ip;
    }

    public void runClient() { //创建反应器线程组
        EventLoopGroup workerLoopGroup = new NioEventLoopGroup();
        try {
            //1 设置反应器 线程组
            b.group(workerLoopGroup);
            //2 设置nio类型的通道
            b.channel(NioSocketChannel.class);
            //3 设置监听端口
            b.remoteAddress(serverIp, serverPort);
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
                    // 客户端发送的是httprequest，所以要使用HttpRequestEncoder进行编码
                    ch.pipeline().addLast(new HttpRequestEncoder());
                    ch.pipeline().addLast(new NettyHttpClientOutboundHandler());
                }
            });

            // Start the client.
//            ChannelFuture f = b.connect(host, port).sync();
//
//
//            f.channel().write(request);
//            f.channel().flush();
//            f.channel().closeFuture().sync();
            ChannelFuture f = b.connect();
//            f.addListener((ChannelFuture futureListener) -> {
//                if (futureListener.isSuccess()) {
//                    Logger.info("EchoClient客户端连接成功!");
//                } else {
//                    Logger.info("EchoClient客户端连接失败!");
//                }
//            });
            // 阻塞,直到连接成功
            f.sync();

//            Channel channel = f.channel();
//            Scanner scanner = new Scanner(System.in);
//            Print.tcfo("请输入发送内容:");
//            while (scanner.hasNext()) {
//                //获取输入的内容
//                String next = scanner.next();
//                byte[] bytes = (Dateutil.getNow() + " >>"
//                        + next).getBytes("UTF-8");
//                //发送ByteBuf
//                ByteBuf buffer = channel.alloc().buffer();
//                buffer.writeBytes(bytes);
//                channel.writeAndFlush(buffer);
//                Print.tcfo("请输入发送内容:");
//            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 从容关闭EventLoopGroup，
            // 释放掉所有资源，包括创建的线程
            workerLoopGroup.shutdownGracefully();
        }
    }
    // main方法
    public static void main(String[] args) throws Exception {
        NettyHttpClient client = new NettyHttpClient("127.0.0.1", 8844);
        client.runClient();
    }
}
