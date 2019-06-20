package com.transformedge.gstr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;


@SpringBootApplication
@ComponentScan(basePackages = "com.transformedge.gstr")
@PropertySources({@PropertySource("file:./config/application.properties")})
public class GstrApplication {

	public static void main(String[] args) {
		SpringApplication.run(GstrApplication.class, args);
	}

}
