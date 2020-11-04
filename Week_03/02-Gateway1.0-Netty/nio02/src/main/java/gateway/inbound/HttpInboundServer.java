package gateway.inbound;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class HttpInboundServer {
    private static Logger logger = LoggerFactory.getLogger(HttpInboundServer.class);

    private int port;

    private String proxyServer;

    public HttpInboundServer(int port, String proxyServer) {
        this.port = port;
        this.proxyServer = proxyServer;
    }

    public void run() throws Exception {

        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup(16);

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.option(ChannelOption.SO_BACKLOG, 128)                   // Delay Task Queue
                    .option(ChannelOption.TCP_NODELAY, true)          // Nagle算法
                    .option(ChannelOption.SO_KEEPALIVE, true)         // 长连接
                    .option(ChannelOption.SO_REUSEADDR, true)         // 重用地址
                    .option(ChannelOption.SO_RCVBUF, 32 * 1024)       // 缓冲区
                    .option(ChannelOption.SO_SNDBUF, 32 * 1024)       // 缓冲区
                    .option(EpollChannelOption.SO_REUSEPORT, true)    // 重用端口
                    .childOption(ChannelOption.SO_KEEPALIVE, true)    // 对worker起作用的，长连接
                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);  // 配置ByteBuff内存池

            b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        //有连接到达时会创建一个通道
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new HttpInboundInitializer(proxyServer));
                        }
                    });

            Channel ch = b.bind(port).sync().channel();
            logger.info("开启netty http服务器，监听地址和端口为 http://127.0.0.1:" + port + '/');
            ch.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
