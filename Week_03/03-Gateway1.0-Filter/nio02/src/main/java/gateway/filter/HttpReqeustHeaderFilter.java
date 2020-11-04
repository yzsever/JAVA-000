package gateway.filter;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;

public class HttpReqeustHeaderFilter implements HttpRequestFilter{
    public void filter(FullHttpRequest fullRequest, ChannelHandlerContext ctx) {
        HttpHeaders headers = fullRequest.headers();
        // 在fullRequest里面添加一个新的key-value，key是nio，value是自己的名字拼音。
        headers.add("nio", "JensonYao");
    }
}
