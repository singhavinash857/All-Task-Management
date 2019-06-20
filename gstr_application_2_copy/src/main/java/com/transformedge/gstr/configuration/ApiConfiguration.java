package com.transformedge.gstr.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Data
@ConfigurationProperties(prefix = "api-configuration")
@Configuration
public class ApiConfiguration {
	
    private List<Api> apis;

    public Api findByName(String name) {
        return apis.stream().filter(a -> a.getName().equals(name))
                    .findFirst().get();
    }
    
    public static class Api {
    	
    	@Setter
    	@Getter
        private String name;
    	
    	@Setter
    	@Getter
        private String url;

        private Map<String, String> headers;

        public Map<String, String> getHeaders() {
            return headers;
        }

        public void setHeaders(Map<String, String> headers) {
            this.headers = headers;
        }

        public String getHeaderValue(String header) {
            return headers.get(header);
        }
       
    }
}
