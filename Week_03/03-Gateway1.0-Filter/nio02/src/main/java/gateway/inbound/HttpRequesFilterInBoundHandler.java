package gateway.inbound;

import gateway.filter.HttpReqeustHeaderFilter;
import gateway.filter.HttpRequestFilter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;

import java.util.ArrayList;
import java.util.List;

public class HttpRequesFilterInBoundHandler extends ChannelInboundHandlerAdapter {

    private List<HttpRequestFilter> filters;

    public HttpRequesFilterInBoundHandler() {
        filters = new ArrayList<HttpRequestFilter>();
        filters.add(new HttpReqeustHeaderFilter());
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            FullHttpRequest fullRequest = (FullHttpRequest) msg;
            // 执行过滤操作
            for(HttpRequestFilter filter : filters){
                filter.filter(fullRequest, ctx);
            }
            // 父类的channelRead()方法会自动调用下一个inBoundHandler的channelRead()方法,
            // 并且会把当前inBoundHandler入站处理器中处理完毕的对象传递到下一个inBoundHandler入站处理器
            super.channelRead(ctx, fullRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}