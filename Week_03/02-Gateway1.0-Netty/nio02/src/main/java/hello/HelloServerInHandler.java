package hello;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

@ChannelHandler.Sharable
public class HelloServerInHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try{
            ByteBuf inMsg = (ByteBuf) msg;
            byte[] bytes = new byte[inMsg.readableBytes()];
            inMsg.readBytes(bytes);
            String inStr = new String(bytes);
            System.out.println("client send msg: " + inStr);

            String response = "i am ok!";
            ByteBuf outMsg = ctx.alloc().buffer(4 * response.length());
            outMsg.writeBytes(response.getBytes());
            ctx.writeAndFlush(outMsg);
        }finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        super.channelReadComplete(ctx);
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        ctx.close();
    }
}
