package com.transformedge.gstr.configuration;

import org.apache.camel.CamelContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GstrConfig {

    @SuppressWarnings("unused")
	@Autowired
    private CamelContext camelContext;

}
