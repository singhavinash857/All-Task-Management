package com.transformedge.gstr.request;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

public class AbstractRequest {
	
	@Setter
	@Getter
    private Map<String, Object> fields;

    protected AbstractRequest() {
        fields = new HashMap<>();
    }

    public void addField(String fieldName, Object fieldValue) {
        fields.put(fieldName, fieldValue);
    }

    public Object getFieldValue(String fieldName) {
        return fields.get(fieldName);
    }
}
