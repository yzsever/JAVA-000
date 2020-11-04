package gateway.router;

import java.util.List;
import java.util.Random;

public class HttpEndpointRandomRouter implements HttpEndpointRouter{
    @Override
    public String route(List<String> endpoints) {
        Random random = new Random();
        int randomPos = random.nextInt(endpoints.size());
        // 随机获得一个后端服务器地址
        String endpoint = endpoints.get(randomPos);
        System.out.println("Select endpoint: "+endpoint);
        return endpoint;
    }
}
