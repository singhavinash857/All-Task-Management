package com.transformedge.apps.appconfiguration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Configuration
@Component
@PropertySource("classpath:application.yml")
public class CsvConfiguration {
	
	@Value( "${spring.data.rest.base-path}" )
	private String baseUrl;
	
	public String getBaseURl(){
		return baseUrl;
	}
	
}
