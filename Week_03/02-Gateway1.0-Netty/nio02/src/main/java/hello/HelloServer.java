package hello;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class HelloServer {
    public void start(int port) {
        ServerBootstrap serverBootstrap = new ServerBootstrap();//注意和 client 的区别
        EventLoopGroup boosGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        serverBootstrap.group(boosGroup, workerGroup);
        serverBootstrap.channel(NioServerSocketChannel.class);//注意和 client 端的区别，client 端是 NioSocketChannel
        serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                ChannelPipeline pipeline = socketChannel.pipeline();
                pipeline.addLast(new HelloServerInHandler());
            }
        });
        serverBootstrap.option(ChannelOption.SO_BACKLOG, 128);
        serverBootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
        try {
            // 绑定端口，开始接收进来的连接
            ChannelFuture f = serverBootstrap.bind(port).sync();
            // 等待服务器  socket 关闭 。
            // 在这个例子中，这不会发生，但你可以优雅地关闭你的服务器。
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            boosGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        HelloServer helloServer = new HelloServer();
        int port = 8080;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }
        helloServer.start(port);
    }
}