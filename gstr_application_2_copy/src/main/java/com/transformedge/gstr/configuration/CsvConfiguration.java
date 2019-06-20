package com.transformedge.gstr.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

import java.util.List;
import java.util.Optional;

@Data
@ConfigurationProperties(prefix = "csv-configuration")
@Configuration
public class CsvConfiguration {
	
    private List<CsvConfig> configurations;

    public CsvConfig findByName(String configName) {
        Optional<CsvConfig> query = configurations.stream()
                                                        .filter(q -> q.getName().equals(configName))
                                                        .findFirst();
        return query.get();
    }

    @Data
    public static class Validation {
        private List<String> numericColumns;

    }

    @Data
    public static class CsvConfig {
        private String name;
        private String inputPath;
        private String outputPath;
        private String userName;
        private String password;
        private String initialDelay;
        private String consumeDelay;
        private Validation validation;
        private TableMetadataConfiguration tableMetadataConfiguration;
        private List<String> outputColumns;

    }

}
