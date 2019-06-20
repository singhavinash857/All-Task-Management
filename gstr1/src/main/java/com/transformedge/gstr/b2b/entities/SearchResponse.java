package com.transformedge.gstr.b2b.entities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;
import lombok.Getter;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SearchResponse {
    private List<Docs> docs;

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
    	
    	@Getter
        @JsonAnySetter
        private Map<String, Object> fields;

        @SuppressWarnings("rawtypes")
		public Docs() {
            this.fields = new HashMap();
        }

        @JsonAnySetter
        public void setField(String key, Object value) {
            this.fields.put(key, value);
        }

    }

}
