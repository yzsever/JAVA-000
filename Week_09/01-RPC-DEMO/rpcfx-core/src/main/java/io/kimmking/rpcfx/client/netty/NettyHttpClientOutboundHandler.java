package io.kimmking.rpcfx.client.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


@ChannelHandler.Sharable
public class NettyHttpClientOutboundHandler extends ChannelInboundHandlerAdapter {

    private String response;

    private CountDownLatch countDownLatch;

    public NettyHttpClientOutboundHandler() {
        this.countDownLatch = new CountDownLatch(1);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            // 将后端服务的结果返回给NettyServer
            FullHttpResponse endpointResponse = (FullHttpResponse) msg;
            ByteBuf buf = endpointResponse.content();
            response = buf.toString(CharsetUtil.UTF_8);
            buf.release();
            countDownLatch.countDown();
            ctx.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    public String getResponse(long timeout, TimeUnit unit) throws InterruptedException {
        countDownLatch.await(3, TimeUnit.SECONDS);
        return response;
    }
}
