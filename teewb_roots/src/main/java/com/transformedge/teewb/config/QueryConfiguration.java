package com.transformedge.teewb.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Optional;

@ConfigurationProperties(prefix = "query-configuration")
@Configuration
public class QueryConfiguration {
    private List<Query> queries;
    private String source;

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public List<Query> getQueries() {
        return queries;
    }

    public void setQueries(List<Query> queries) {
        this.queries = queries;
    }

    public boolean isJdbcSource() {
        return "JDBC".equalsIgnoreCase(source);
    }

    public boolean isCsvSource() {
        return "CSV".equalsIgnoreCase(source);
    }

    public Query findByName(String name) {
        Optional<Query> query = queries.stream()
                                        .filter(q -> q.getName().equals(name))
                                        .findFirst();
        return query.get();
    }

    public static class Query {
        private String name;
        private String query;
        private String onConsumeInvoiceQuery;
        private String onConsumeFailInvoiceQuery;
        private String initialDelay;
        private String consumeDelay;
        private String onExceptionInvoiceQuery;
        private String updateTrackingAndBatchQuery;

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

        public String getOnConsumeInvoiceQuery() {
            return onConsumeInvoiceQuery;
        }

        public void setOnConsumeInvoiceQuery(String onConsumeInvoiceQuery) {
            this.onConsumeInvoiceQuery = onConsumeInvoiceQuery;
        }

        public String getOnConsumeFailInvoiceQuery() {
            return onConsumeFailInvoiceQuery;
        }

        public void setOnConsumeFailInvoiceQuery(String onConsumeFailInvoiceQuery) {
            this.onConsumeFailInvoiceQuery = onConsumeFailInvoiceQuery;
        }


        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }

        public String getUpdateTrackingAndBatchQuery() {
            return updateTrackingAndBatchQuery;
        }

        public void setUpdateTrackingAndBatchQuery(String updateTrackingAndBatchQuery) {
            this.updateTrackingAndBatchQuery = updateTrackingAndBatchQuery;
        }

        public String getOnExceptionInvoiceQuery() {
            return onExceptionInvoiceQuery;
        }

        public void setOnExceptionInvoiceQuery(String onExceptionInvoiceQuery) {
            this.onExceptionInvoiceQuery = onExceptionInvoiceQuery;
        }

    }

}
