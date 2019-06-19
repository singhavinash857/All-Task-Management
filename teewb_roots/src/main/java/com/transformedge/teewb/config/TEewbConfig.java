package com.transformedge.teewb.config;

import org.apache.camel.CamelContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

/**
 * Created by chandrasekarramaswamy on 15/04/18.
 */
@Configuration
public class TEewbConfig {

    @Autowired
    private CamelContext camelContext;

}
