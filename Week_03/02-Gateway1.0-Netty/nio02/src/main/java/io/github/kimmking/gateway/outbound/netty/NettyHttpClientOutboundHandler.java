package io.github.kimmking.gateway.outbound.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.util.ReferenceCountUtil;
import org.apache.http.util.EntityUtils;

import java.util.Map;

import static io.netty.handler.codec.http.HttpResponseStatus.NO_CONTENT;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

@ChannelHandler.Sharable
public class NettyHttpClientOutboundHandler extends ChannelInboundHandlerAdapter {

    private ChannelHandlerContext serverCtx;
    private FullHttpRequest inbound;
    private String url;


    public NettyHttpClientOutboundHandler(FullHttpRequest request, ChannelHandlerContext ctx, String url) {
        this.inbound = request;
        this.serverCtx = ctx;
        this.url = url;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        try {
            FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_0, HttpMethod.GET, url);
            for (Map.Entry<String, String> entry : inbound.headers()) {
                request.headers().add(entry.getKey(), entry.getValue());
            }
            ctx.writeAndFlush(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            FullHttpResponse endpointResponse = (FullHttpResponse) msg;
            ByteBuf buf = endpointResponse.content();
            byte[] responseBuf = new byte[buf.readableBytes()];
            buf.readBytes(responseBuf);
            handleResponse(inbound, serverCtx, responseBuf);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleResponse(final FullHttpRequest fullRequest, final ChannelHandlerContext ctx, final byte[] endpointResponse) throws Exception {
        FullHttpResponse response = null;
        try {
            System.out.println("NettyHttpClient:"+new String(endpointResponse));
            response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(endpointResponse));
            response.headers().set("Content-Type", "application/json");
            response.headers().setInt("Content-Length", response.content().readableBytes());
        } catch (Exception e) {
            e.printStackTrace();
            response = new DefaultFullHttpResponse(HTTP_1_1, NO_CONTENT);
            exceptionCaught(ctx, e);
        } finally {
            if (fullRequest != null) {
                if (!HttpUtil.isKeepAlive(fullRequest)) {
                    serverCtx.write(response).addListener(ChannelFutureListener.CLOSE);
                } else {
                    serverCtx.write(response);
                }
            }
            serverCtx.flush();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}