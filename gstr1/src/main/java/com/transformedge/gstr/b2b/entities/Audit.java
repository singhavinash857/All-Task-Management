package com.transformedge.gstr.b2b.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Audit {
    @JsonProperty("_id")
    private String trackingId;
    private String type;
    private String invoiceId;
    @JsonRawValue
    private String request;
    @JsonRawValue
    private String response;

    @JsonProperty(value = "request")
    public void setRequestRaw(JsonNode jsonNode) {
        setRequest(jsonNode.toString());
    }

    @JsonProperty(value = "response")
    public void setResponseRaw(JsonNode jsonNode) {
        setResponse(jsonNode.toString());
    }

    public String toJSON() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }



}
