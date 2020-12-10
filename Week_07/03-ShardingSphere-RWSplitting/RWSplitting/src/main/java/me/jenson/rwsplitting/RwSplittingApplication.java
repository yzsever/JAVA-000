package me.jenson.rwsplitting;

import me.jenson.rwsplitting.service.ExampleService;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.transaction.jta.JtaAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import java.sql.SQLException;

@ComponentScan("me.jenson")
@MapperScan(basePackages = "me.jenson.repository")
@SpringBootApplication(exclude = JtaAutoConfiguration.class)
public class RwSplittingApplication {

	public static void main(final String[] args) throws SQLException {
		try (ConfigurableApplicationContext applicationContext = SpringApplication.run(RwSplittingApplication.class, args)) {
			ExampleExecuteTemplate.run(applicationContext.getBean(ExampleService.class));
		}
	}
}
