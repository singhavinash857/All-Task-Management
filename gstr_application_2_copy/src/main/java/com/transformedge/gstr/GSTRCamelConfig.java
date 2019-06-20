package com.transformedge.gstr;

import org.apache.camel.CamelContext;
import org.apache.camel.spring.boot.CamelContextConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.transformedge.gstr.configuration.QueryConfiguration;

@Configuration
public class GSTRCamelConfig {

    @Autowired
    private QueryConfiguration queryConfiguration;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Bean
    public CamelContextConfiguration contextConfiguration() {

        return new CamelContextConfiguration() {
            @Override
            public void beforeApplicationStart(CamelContext camelContext) {
                System.out.println("METHOD CALLED BEFORE APPLICATION START !!!!!");
            }

            @Override
            public void afterApplicationStart(CamelContext camelContext) {
                System.out.println("METHOD CALLED AFTER APPLICATION START !!!!");

                // getting source csv or any type of database...
                String source = queryConfiguration.getSource();

                try {
                    // starting route service according to the source..
                    switch (source) {
                        case "JDBC":
                            logger.info("CONFIGURED SOURCE IS JDBC !!");
                            camelContext.startRoute("JDBCProcessRoute");
                            break;
                        case "CSV":
                            logger.info("CONFIGURED SOURCE IS CSV !!");
                            camelContext.startRoute("CSVPoller");
                            break;
                        default:
                            logger.error("Invalid Source configured. Please check!!!!");

                    }
                } catch (Exception ex) {
                    logger.error("Exception occured when starting application", ex);
                }
            }
        };


    }



}
