package io.github.kimmking.gateway.outbound.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class NettyHttpClientOutboundHandler extends ChannelInboundHandlerAdapter {

    public static final NettyHttpClientOutboundHandler INSTANCE = new NettyHttpClientOutboundHandler();

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {


    }


    /**
     * 出站处理方法 *
     * @param ctx 上下文
     * @param msg 入站数据包
     * @throws Exception 可能抛出的异常
     * */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = (ByteBuf) msg;
        int len = byteBuf.readableBytes();
        byte[] arr = new byte[len];
        byteBuf.getBytes(0, arr);
        //Logger.info("client received: " + new String(arr, "UTF-8"));
        // 释放ByteBuf的两种方法
        // 方法一:手动释放ByteBuf
        byteBuf.release();
        //方法二:调用父类的入站方法，将msg向后传递
        // super.channelRead(ctx,msg);
    }
}