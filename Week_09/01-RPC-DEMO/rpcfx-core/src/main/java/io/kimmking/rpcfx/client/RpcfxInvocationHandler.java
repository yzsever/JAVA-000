package io.kimmking.rpcfx.client;

import com.alibaba.fastjson.JSON;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import io.kimmking.rpcfx.api.RpcfxRequest;
import io.kimmking.rpcfx.api.RpcfxResponse;
import io.kimmking.rpcfx.client.netty.NettyHttpClient;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class RpcfxInvocationHandler implements InvocationHandler {

    public static final MediaType JSONTYPE = MediaType.get("application/json; charset=utf-8");

    private final Class<?> serviceClass;
    private final String url;
    public <T> RpcfxInvocationHandler(Class<T> serviceClass, String url) {
        this.serviceClass = serviceClass;
        this.url = url;
    }

    // 可以尝试，自己去写对象序列化，二进制还是文本的，，，rpcfx是xml自定义序列化、反序列化，json: code.google.com/p/rpcfx
    // int byte char float double long bool
    // [], data class
    private final XStream xstream = new XStream(new StaxDriver());
    // 1.可以复用client
    // 2.尝试使用httpclient或者netty client
    private final OkHttpClient client = new OkHttpClient();

    private final NettyHttpClient nettyHttpClient = new NettyHttpClient();

    @Override
    public Object invoke(Object proxy, Method method, Object[] params) throws Throwable {
        RpcfxRequest request = new RpcfxRequest();
        request.setServiceClass(this.serviceClass.getName());
        request.setMethod(method.getName());
        request.setParams(params);
        // 使用Netty调用后端服务
        String responseXML = nettyPost(request, url);
        RpcfxResponse response = (RpcfxResponse) xstream.fromXML(responseXML);

        // 这里判断response.status，处理异常
        if (response.isStatus()) {
            return response.getResult();
        }
        // 考虑封装一个全局的RpcfxException
        throw response.getRpcfxException();
    }

    private String post(RpcfxRequest req, String url) throws IOException {
        String reqJson = JSON.toJSONString(req);
        System.out.println("req json: "+reqJson);

        final Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(JSONTYPE, reqJson))
                .build();
        String respJson = client.newCall(request).execute().body().string();
        System.out.println("resp json: "+respJson);
        // return JSON.parseObject(respJson, RpcfxResponse.class);
        return respJson;
    }

    private String nettyPost(RpcfxRequest req, String url) throws IOException {
        String reqJson = JSON.toJSONString(req);
        System.out.println("NettyPost req json: "+reqJson);
        String respJson = nettyHttpClient.handle(reqJson, url);
        System.out.println("NettyPost resp json: "+respJson);
        // return JSON.parseObject(respJson, RpcfxResponse.class);
        return respJson;
    }
}