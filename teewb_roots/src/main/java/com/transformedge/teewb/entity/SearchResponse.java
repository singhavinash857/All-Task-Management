package com.transformedge.teewb.entity;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chandrasekarramaswamy on 21/05/18.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SearchResponse {
    private List<Docs> docs;

    public List<Docs> getDocs() {
        return docs;
    }

    @JsonAnySetter
    public void setDocs(List<Docs> docs) {
        this.docs = docs;
    }

    public <T> List<T> extractResponse(Class<T> type) {
        ObjectMapper mapper = new ObjectMapper();
        List<T> results = new ArrayList<T>(docs.size());
        for (Docs docs : getDocs()) {
            T t = mapper.convertValue(docs.getFields(), type);
            results.add(t);
        }
        return results;
    }

    public static SearchResponse fromJSON(String json) {
        ObjectMapper mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            return mapper.readValue(json, SearchResponse.class);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static class Docs {
        @JsonAnySetter
        private Map<String, Object> fields;

        public Docs() {
            this.fields = new HashMap();
        }

        //@JsonValue
        public Map<String, Object> getFields() {
            return fields;
        }

        @JsonAnySetter
        public void setField(String key, Object value) {
            this.fields.put(key, value);
        }

    }

    public static void main(String args[]) throws Exception {
        ObjectMapper mapper = new ObjectMapper()
                                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        SearchResponse searchResponse = mapper.readValue(
                    new File("/Users/chandrasekarramaswamy/Projects/TEewb/config/SearchResponse.json"),
                    SearchResponse.class);
        List<TransactionAudit> transactionAudit =  searchResponse.extractResponse(TransactionAudit.class);
        System.out.println("trx id is: " + transactionAudit.get(0).getTrxId());
    }
}
