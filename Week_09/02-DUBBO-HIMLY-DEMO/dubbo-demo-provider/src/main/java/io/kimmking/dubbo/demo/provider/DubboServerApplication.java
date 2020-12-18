package io.kimmking.dubbo.demo.provider;

import org.dromara.hmily.spring.annotation.RefererAnnotationBeanPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class DubboServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(DubboServerApplication.class, args);
	}

	@Bean
	public BeanPostProcessor refererAnnotationBeanPostProcessor() {
		return new RefererAnnotationBeanPostProcessor();
	}
}
