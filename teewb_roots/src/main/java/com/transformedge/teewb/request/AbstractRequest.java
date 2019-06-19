package com.transformedge.teewb.request;

import java.util.HashMap;
import java.util.Map;

public class AbstractRequest {
    private Map<String, Object> fields;

    protected AbstractRequest() {
        fields = new HashMap<>();
    }

    public void setFields(Map<String, Object> fields) {
        this.fields = fields;
    }

    public Map<String, Object> getFields() {
        return fields;
    }

    public void addField(String fieldName, Object fieldValue) {
        fields.put(fieldName, fieldValue);
    }

    public Object getFieldValue(String fieldName) {
        return fields.get(fieldName);
    }
}
