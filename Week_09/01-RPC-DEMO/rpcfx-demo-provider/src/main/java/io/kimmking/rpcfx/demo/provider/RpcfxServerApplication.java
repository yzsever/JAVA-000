package io.kimmking.rpcfx.demo.provider;

import io.kimmking.rpcfx.api.RpcfxRequest;
import io.kimmking.rpcfx.api.RpcfxResolver;
import io.kimmking.rpcfx.api.RpcfxResponse;
import io.kimmking.rpcfx.demo.api.OrderService;
import io.kimmking.rpcfx.demo.api.UserService;
import io.kimmking.rpcfx.server.RpcfxInvoker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class RpcfxServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(RpcfxServerApplication.class, args);
	}

	@Autowired
	RpcfxInvoker invoker;

	@PostMapping("/old")
	public RpcfxResponse oldInvoke(@RequestBody RpcfxRequest request) {
		return invoker.oldInvoke(request);
	}

	@PostMapping("/")
	public String invoke(@RequestBody RpcfxRequest request) {
		return invoker.invoke(request); //由xstreamresponse转成xml的形式,client端接收后需要转成对象
	}

	@Bean
	public RpcfxInvoker createInvoker(@Autowired RpcfxResolver resolver){
		return new RpcfxInvoker(resolver);
	}

	@Bean
	public RpcfxResolver createResolver(){
		return new DemoResolver();
	}

	// 能否去掉name ===> getBean By Type
	// @Bean(name = "io.kimmking.rpcfx.demo.api.UserService")
	@Bean
	public UserService createUserService(){
		return new UserServiceImpl();
	}

	//@Bean(name = "io.kimmking.rpcfx.demo.api.OrderService")
	@Bean
	public OrderService createOrderService(){
		return new OrderServiceImpl();
	}

}
