package io.github.kimmking.gateway.inbound;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HttpInboundHandler extends ChannelInboundHandlerAdapter {

    private static Logger logger = LoggerFactory.getLogger(HttpInboundHandler.class);
    private final String proxyServer;
    private static final Map<ChannelId, Channel> channelMap = new ConcurrentHashMap<>();

    public HttpInboundHandler(String proxyServer) {
        this.proxyServer = proxyServer;
    }

    //连接处于活动状态
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        channelMap.put(ctx.channel().id(), channel);
        System.out.println(channel.remoteAddress() + " 上线了");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        channelMap.remove(channel);
        System.out.println(channel.remoteAddress() +" 下线了");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try{
//            ByteBuf inMsg = (ByteBuf) msg;
//            byte[] bytes = new byte[inMsg.readableBytes()];
//            inMsg.readBytes(bytes);
//            String inStr = new String(bytes);
//            System.out.println("server send msg: " + inStr);
//
//            String response = "i am ok!";
//            ByteBuf outMsg = ctx.alloc().buffer(4 * response.length());
//            outMsg.writeBytes(response.getBytes());
           // ctx.writeAndFlush(outMsg);
            FullHttpRequest fullRequest = (FullHttpRequest) msg;
            String uri = fullRequest.uri();
            System.out.println("接收到的请求url为{}"+ uri);
            //ChannelFuture f = ctx.writeAndFlush(msg);
            for(Channel ch : channelMap.values()){
                try {
                    ChannelFuture f = ch.writeAndFlush(msg);
                }catch (Exception e){
                    e.printStackTrace();
                }

//                f.addListener((ChannelFuture futureListener) -> {
//                    System.out.println("写回后，msg.refCnt:");
//                });
            }
        }finally {
            //ReferenceCountUtil.release(msg);
        }
    }

//    @Override
//    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
//        super.channelReadComplete(ctx);
//        ctx.flush();
//    }
//
//    @Override
//    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
//        super.exceptionCaught(ctx, cause);
//        ctx.close();
//    }

//    @Override
//    public void channelRead(ChannelHandlerContext ctx, Object msg) {
//        try {
//            //logger.info("channelRead流量接口请求开始，时间为{}", startTime);
//           // FullHttpRequest fullRequest = (FullHttpRequest) msg;
////            String uri = fullRequest.uri();
////            //logger.info("接收到的请求url为{}", uri);
////            if (uri.contains("/test")) {
////                handlerTest(fullRequest, ctx);
////            }
//            //handler.handle(fullRequest, ctx);
////            System.out.println("入站处理器被回调");
////            //写回数据，异步任务
////            //System.out.println("写回前，msg.refCnt:" + ((ByteBuf) msg).refCnt());
////            ChannelFuture f = ctx.writeAndFlush(msg);
////            f.addListener((ChannelFuture futureListener) -> {
////                //System.out.println("写回后，msg.refCnt:" + ((ByteBuf) msg).refCnt());
////            });
//            System.out.println("入站处理器被回调");
//            ByteBuf inMsg = (ByteBuf) msg;
//            byte[] bytes = new byte[inMsg.readableBytes()];
//            inMsg.readBytes(bytes);
//            String inStr = new String(bytes);
//            System.out.println("client send msg: " + inStr);
//
//            String response = "i am ok!";
//            ByteBuf outMsg = ctx.alloc().buffer(4 * response.length());
//            outMsg.writeBytes(response.getBytes());
//            ctx.writeAndFlush(outMsg);
//        } catch(Exception e) {
//            e.printStackTrace();
//        } finally {
//            ReferenceCountUtil.release(msg);
//        }
//    }

//    private void handlerTest(FullHttpRequest fullRequest, ChannelHandlerContext ctx) {
//        FullHttpResponse response = null;
//        try {
//            String value = "hello,kimmking";
//            response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(value.getBytes("UTF-8")));
//            response.headers().set("Content-Type", "application/json");
//            response.headers().setInt("Content-Length", response.content().readableBytes());
//
//        } catch (Exception e) {
//            logger.error("处理测试接口出错", e);
//            response = new DefaultFullHttpResponse(HTTP_1_1, NO_CONTENT);
//        } finally {
//            if (fullRequest != null) {
//                if (!HttpUtil.isKeepAlive(fullRequest)) {
//                    ctx.write(response).addListener(ChannelFutureListener.CLOSE);
//                } else {
//                    response.headers().set(CONNECTION, KEEP_ALIVE);
//                    ctx.write(response);
//                }
//            }
//        }
//    }
//
}
