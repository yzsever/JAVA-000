package gateway.outbound.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.util.ReferenceCountUtil;

import static io.netty.handler.codec.http.HttpResponseStatus.NO_CONTENT;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

@ChannelHandler.Sharable
public class NettyHttpClientOutboundHandler extends ChannelInboundHandlerAdapter {
    private ChannelHandlerContext serverCtx;

    public NettyHttpClientOutboundHandler(ChannelHandlerContext ctx) {
        this.serverCtx = ctx;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            // 将后端服务的结果返回给NettyServer
            FullHttpResponse endpointResponse = (FullHttpResponse) msg;
            ByteBuf buf = endpointResponse.content();
            byte[] responseBuf = new byte[buf.readableBytes()];
            buf.readBytes(responseBuf);
            handleResponse(null, serverCtx, responseBuf);
            ctx.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    private void handleResponse(final FullHttpRequest fullRequest, final ChannelHandlerContext ctx, final byte[] endpointResponse) throws Exception {
        FullHttpResponse response = null;
        try {
            System.out.println("NettyHttpClient:" + new String(endpointResponse));
            response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(endpointResponse));
            response.headers().set("Content-Type", "application/json");
            response.headers().setInt("Content-Length", endpointResponse.length);
        } catch (Exception e) {
            e.printStackTrace();
            response = new DefaultFullHttpResponse(HTTP_1_1, NO_CONTENT);
            exceptionCaught(ctx, e);
        } finally {
            if (fullRequest != null) {
                if (!HttpUtil.isKeepAlive(fullRequest)) {
                    ctx.write(response).addListener(ChannelFutureListener.CLOSE);
                } else {
                    ctx.write(response);
                }
            } else {
                // 将响应返回给Server
                ctx.write(response).addListener(ChannelFutureListener.CLOSE);
            }
            ctx.flush();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
