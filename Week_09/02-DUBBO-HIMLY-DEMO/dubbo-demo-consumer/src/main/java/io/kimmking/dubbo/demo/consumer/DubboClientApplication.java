package io.kimmking.dubbo.demo.consumer;


import io.kimmking.dubbo.demo.consumer.service.CurrencyTradeService;
import org.dromara.hmily.spring.annotation.RefererAnnotationBeanPostProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;

@ImportResource({"classpath:spring-dubbo.xml"})
@SpringBootApplication
public class DubboClientApplication {

	@Autowired
	private CurrencyTradeService currencyTradeService;

	public static void main(String[] args) {
		SpringApplication.run(DubboClientApplication.class).close();
	}

	@Bean
	public ApplicationRunner runner() {
		return args -> {
			System.out.println("=============Start CurrencyTradeService userAAndBCurrenyTrade===========");
			currencyTradeService.userAAndBCurrenyTrade();
			System.out.println("=============End CurrencyTradeService userAAndBCurrenyTrade===========");
		};
	}
}
