package com.transformedge.gstr.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "table-metadata-configuration")
@Configuration
public class TableMetadataConfiguration {
    private List<Table> tables;

    public Table findByName(String name) {
        return tables.stream().filter(t -> t.getName().equals(name))
                .findFirst().get();
    }

    @Data
    public static class Table {
        private String name;
        private String identifierColumn;
        private List<String> columns;

    }

}
