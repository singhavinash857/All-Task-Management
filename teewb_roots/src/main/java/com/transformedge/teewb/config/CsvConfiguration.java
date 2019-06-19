package com.transformedge.teewb.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Optional;

/**
 * Created by chandrasekarramaswamy on 14/06/18.
 */
@ConfigurationProperties(prefix = "csv-configuration")
@Configuration
public class CsvConfiguration {
    private List<CsvConfig> configurations;


    public List<CsvConfig> getConfigurations() {
        return configurations;
    }

    public void setConfigurations(List<CsvConfig> configurations) {
        this.configurations = configurations;
    }

    public CsvConfig findByName(String configName) {
        Optional<CsvConfig> query = configurations.stream()
                                                        .filter(q -> q.getName().equals(configName))
                                                        .findFirst();
        return query.get();
    }

    public static class Validation {
        private List<String> numericColumns;

        public List<String> getNumericColumns() {
            return numericColumns;
        }

        public void setNumericColumns(List<String> numericColumns) {
            this.numericColumns = numericColumns;
        }
    }

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

        public TableMetadataConfiguration getTableMetadataConfiguration() {
            return tableMetadataConfiguration;
        }

        public void setTableMetadataConfiguration(TableMetadataConfiguration tableMetadataConfiguration) {
            this.tableMetadataConfiguration = tableMetadataConfiguration;
        }

        public List<String> getOutputColumns() {
            return outputColumns;
        }

        public void setOutputColumns(List<String> outputColumns) {
            this.outputColumns = outputColumns;
        }

        public String getInitialDelay() {
            return initialDelay;
        }

        public void setInitialDelay(String initialDelay) {
            this.initialDelay = initialDelay;
        }

        public String getConsumeDelay() {
            return consumeDelay;
        }

        public void setConsumeDelay(String consumeDelay) {
            this.consumeDelay = consumeDelay;
        }

        public Validation getValidation() {
            return validation;
        }

        public void setValidation(Validation validation) {
            this.validation = validation;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getInputPath() {
            return inputPath;
        }

        public void setInputPath(String inputPath) {
            this.inputPath = inputPath;
        }

        public String getOutputPath() {
            return outputPath;
        }

        public void setOutputPath(String outputPath) {
            this.outputPath = outputPath;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

}
