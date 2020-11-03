package hello;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class HelloClient {
    public void connect(String host, int port) {
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap(); //注意和 server 的区别
        bootstrap.group(eventLoopGroup);
        bootstrap.channel(NioSocketChannel.class);//注意和 server 端的区别，server 端是 NioServerSocketChannel
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                ChannelPipeline pipeline = socketChannel.pipeline();
                pipeline.addLast(new HelloClientIntHandler());
            }
        });
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);

        try {
            // Start the client.
            ChannelFuture future = bootstrap.connect(host, port).sync();
            // 等待服务器  socket 关闭 。
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            eventLoopGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        HelloClient client = new HelloClient();
        client.connect("127.0.0.1", 8080);
    }
}
