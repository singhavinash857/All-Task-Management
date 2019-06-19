package com.transformedge.apps;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/*
 *  Created By Avinash Singh
 *  Date : 19-march-2019
 */
@SpringBootApplication
public class LoginAppsApplication extends SpringBootServletInitializer{

	@Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(LoginAppsApplication.class);	
    }
	public static void main(String[] args) {
		SpringApplication.run(LoginAppsApplication.class, args);
	}

}
