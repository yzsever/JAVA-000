package gateway.router;

import java.util.ArrayList;
import java.util.List;

public class EndpointFactory {
    // 通过单例模式创建工厂类
    private final static EndpointFactory INSTACNE = new EndpointFactory();
    private final HttpEndpointRandomRouter router = new HttpEndpointRandomRouter();
    private List<String> endpoints = new ArrayList<>();

    // 配置可用后端服务
    public EndpointFactory(){
        endpoints.add("http://127.0.0.1:8807");
        endpoints.add("http://127.0.0.1:8808");
        endpoints.add("http://127.0.0.1:8809");
    }

    // 调用随机路由获取一个后端服务
    public String getEndpoint(){
        return router.route(endpoints);
    }

    public static EndpointFactory getInstance(){
        return INSTACNE;
    }
}
