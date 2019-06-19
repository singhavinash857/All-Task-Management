package com.transformedge.teewb;

import com.transformedge.teewb.config.QueryConfiguration;
import org.apache.camel.CamelContext;
import org.apache.camel.spring.boot.CamelContextConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class EwayBillCamelConfig {

    @Autowired
    private QueryConfiguration queryConfiguration;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Bean
    public CamelContextConfiguration contextConfiguration() {

        return new CamelContextConfiguration() {
            @Override
            public void beforeApplicationStart(CamelContext camelContext) {
                System.out.println("Invoked beforeAppStart!!!!!");
            }

            @Override
            public void afterApplicationStart(CamelContext camelContext) {
                System.out.println("Invoked after app start!!!!");

                // getting source csv or any type of database...
                String source = queryConfiguration.getSource();

                try {
                    // starting route service according to the source..
                    switch (source) {
                        case "JDBC":
                            logger.info("Configured source is JDBC");
                            camelContext.startRoute("EwayBillJDBCProcessRoute");
                            break;
                        case "CSV":
                            logger.info("Configured source is CSV");
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
