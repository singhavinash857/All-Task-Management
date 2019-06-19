package com.transformedge.teewb.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "api-configuration")
@Configuration
public class ApiConfiguration {
    private List<Api> apis;

    public List<Api> getApis() {
        return apis;
    }

    public void setApis(List<Api> apis) {
        this.apis = apis;
    }

    public Api findByName(String name) {
        return apis.stream().filter(a -> a.getName().equals(name))
                    .findFirst().get();
    }

    public static class Api {
        private String name;
        private Map<String, String> headers;
        private String url;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Map<String, String> getHeaders() {
            return headers;
        }

        public void setHeaders(Map<String, String> headers) {
            this.headers = headers;
        }

        public String getHeaderValue(String header) {
            return headers.get(header);
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }
}
