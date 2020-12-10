package me.jenson;

import me.jenson.service.ExampleService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.sql.SQLException;

@SpringBootApplication
public class DynamicSwitchDataSourceApplication {

	public static void main(final String[] args) throws SQLException {
		try (ConfigurableApplicationContext applicationContext = SpringApplication.run(DynamicSwitchDataSourceApplication.class, args)) {
			ExampleExecuteTemplate.run(applicationContext.getBean(ExampleService.class));
		}
	}
}
