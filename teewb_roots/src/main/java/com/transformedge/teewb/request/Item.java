package com.transformedge.teewb.request;

import java.util.Map;

public class Item extends AbstractRequest {

    public Item() {
    }

    public Item(Map<String, Object> fields) {
        super();
        this.setFields(fields);
    }

    @Override
    public String toString() {
        return "[Item: " + getFields().toString() + "]";
    }
}
