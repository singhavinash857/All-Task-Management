package com.transformedge.teewb.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@ConfigurationProperties(prefix = "table-metadata-configuration")
@Configuration
public class TableMetadataConfiguration {
    private List<Table> tables;

    public List<Table> getTables() {
        return tables;
    }

    public void setTables(List<Table> tables) {
        this.tables = tables;
    }

    public Table findByName(String name) {
        return tables.stream().filter(t -> t.getName().equals(name))
                .findFirst().get();
    }

    public static class Table {
        private String name;
        private List<String> columns;
        private String identifierColumn;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<String> getColumns() {
            return columns;
        }

        public void setColumns(List<String> columns) {
            this.columns = columns;
        }

        public String getIdentifierColumn() {
            return identifierColumn;
        }

        public void setIdentifierColumn(String identifierColumn) {
            this.identifierColumn = identifierColumn;
        }
    }

}
