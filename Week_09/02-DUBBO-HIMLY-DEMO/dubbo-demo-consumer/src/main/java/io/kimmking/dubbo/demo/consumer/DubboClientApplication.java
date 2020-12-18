package io.kimmking.dubbo.demo.consumer;

import io.kimmking.dubbo.demo.api.ChangeIntoCNHService;
import io.kimmking.dubbo.demo.api.ChangeIntoUSDService;
import io.kimmking.dubbo.demo.api.Order;
import io.kimmking.dubbo.demo.api.OrderService;
import io.kimmking.dubbo.demo.api.User;
import io.kimmking.dubbo.demo.api.UserService;
import io.kimmking.dubbo.demo.consumer.service.CurrencyTradeService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.dromara.hmily.spring.annotation.RefererAnnotationBeanPostProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class DubboClientApplication {

	@DubboReference(version = "1.0.0", url = "dubbo://127.0.0.1:12345")
	private UserService userService;

	@DubboReference(version = "1.0.0", url = "dubbo://127.0.0.1:12345")
	private OrderService orderService;

	@Autowired
	private CurrencyTradeService currencyTradeService;

	public static void main(String[] args) {
		SpringApplication.run(DubboClientApplication.class).close();
	}

	@Bean
	public ApplicationRunner runner() {
		return args -> {
			User user = userService.findById(1);
			System.out.println("find user id=1 from server: " + user.getName());
			Order order = orderService.findOrderById(1992129);
			System.out.println(String.format("find order name=%s, amount=%f",order.getName(),order.getAmount()));
			System.out.println("=============Start CurrencyTradeService userAAndBCurrenyTrade===========");
			currencyTradeService.userAAndBCurrenyTrade();
			System.out.println("=============End CurrencyTradeService userAAndBCurrenyTrade===========");
		};
	}

	@Bean
	public BeanPostProcessor refererAnnotationBeanPostProcessor() {
		return new RefererAnnotationBeanPostProcessor();
	}

}
