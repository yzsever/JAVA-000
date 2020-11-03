package hello;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

public class HelloClientIntHandler extends ChannelInboundHandlerAdapter {

    // 连接成功后，向server发送消息
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("connected server. start send msg.");
        String msg = "r u ok?";
        ByteBuf encoded = ctx.alloc().buffer(4 * msg.length());
        encoded.writeBytes(msg.getBytes());
        ctx.writeAndFlush(encoded);
    }

    // 接收server端的消息，并打印出来
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            ByteBuf result = (ByteBuf) msg;
            byte[] serverMsg = new byte[result.readableBytes()];
            result.readBytes(serverMsg);
            System.out.println("Server said:" + new String(serverMsg));
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }
}
