package com.transformedge.teewb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

@SpringBootApplication
@ComponentScan(basePackages = "com.transformedge.teewb")
@PropertySources({@PropertySource("file:./config/application.properties")})
public class Application {

    public static void main(String args[]) {
        SpringApplication.run(Application.class, args);
    }

}
