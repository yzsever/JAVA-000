package echoServer;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * create by 尼恩 @ 疯狂创客圈
 **/
@ChannelHandler.Sharable
public class NettyEchoServerHandler extends ChannelInboundHandlerAdapter {
    public static final NettyEchoServerHandler INSTANCE = new NettyEchoServerHandler();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf in = (ByteBuf) msg;
        System.out.println("msg type: " + (in.hasArray()?"堆内存":"直接内存"));

        int len = in.readableBytes();
        byte[] arr = new byte[len];
        in.getBytes(0, arr);
        System.out.println("server received: " + new String(arr, "UTF-8"));

        //写回数据，异步任务
        System.out.println("写回前，msg.refCnt:" + ((ByteBuf) msg).refCnt());
        ChannelFuture f = ctx.writeAndFlush(msg);
        f.addListener((ChannelFuture futureListener) -> {
            System.out.println("写回后，msg.refCnt:" + ((ByteBuf) msg).refCnt());
        });
    }
}
