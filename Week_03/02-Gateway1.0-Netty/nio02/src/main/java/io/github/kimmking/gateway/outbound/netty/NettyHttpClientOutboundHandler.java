package io.github.kimmking.gateway.outbound.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.ReferenceCountUtil;

@ChannelHandler.Sharable
public class NettyHttpClientOutboundHandler extends ChannelInboundHandlerAdapter {
    public static final NettyHttpClientOutboundHandler INSTANCE = new NettyHttpClientOutboundHandler();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
//            ByteBuf inMsg = (ByteBuf) msg;
//            byte[] bytes = new byte[inMsg.readableBytes()];
//            inMsg.readBytes(bytes);
//            String inStr = new String(bytes);
//            System.out.println("server send msg: " + inStr);

//            String response = "i am ok!";
//            ByteBuf outMsg = ctx.alloc().buffer(4 * response.length());
//            outMsg.writeBytes(response.getBytes());
//            ctx.writeAndFlush(outMsg);
            FullHttpRequest fullRequest = (FullHttpRequest) msg;
            String uri = fullRequest.uri();
            System.out.println("接收到的请求url为{}" + uri);
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        System.out.println("被调用:handlerAdded()");
        super.handlerAdded(ctx);
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        System.out.println("被调用:channelRegistered()");
        super.channelRegistered(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("被调用:channelActive()");
        super.channelActive(ctx);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        System.out.println("被调用:channelReadComplete()");
        super.channelReadComplete(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("被调用:channelInactive()");
        super.channelInactive(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        System.out.println("被调用:	channelUnregistered()");
        super.channelUnregistered(ctx);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        System.out.println("被调用:handlerRemoved()");
        super.handlerRemoved(ctx);
    }
}