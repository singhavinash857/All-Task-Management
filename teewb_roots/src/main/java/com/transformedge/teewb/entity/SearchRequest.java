package com.transformedge.teewb.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by chandrasekarramaswamy on 21/05/18.
 */

public class SearchRequest {

    @JsonProperty("selector")
    private Selector selector;

    public Selector getSelector() {
        return selector;
    }

    public void setSelector(Selector selector) {
        this.selector = selector;
    }

    public static class Selector {

        private Map<String, Object> fields;

        @JsonValue
        public Map<String, Object> getFields() {
            return fields;
        }

        public void setFields(Map<String, Object> fields) {
            this.fields = fields;
        }
    }

    public String toJSON() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static void main(String args[]) throws Exception {
        SearchRequest searchRequest = new SearchRequest();
        Selector selector = new Selector();
        Map<String, Object> fields = new HashMap();
        fields.put("trxIds", Arrays.asList(300362, 300363));
        selector.setFields(fields);
        searchRequest.setSelector(selector);

        ObjectMapper mapper = new ObjectMapper();
        //mapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
        try {
            System.out.println( mapper.writeValueAsString(searchRequest));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}
