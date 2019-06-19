package com.transformedge.teewb.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.transformedge.teewb.request.Invoice;
import com.transformedge.teewb.request.OutputResponse;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

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

    public String getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(String invoiceId) {
        this.invoiceId = invoiceId;
    }

    public String getTrackingId() {
        return trackingId;
    }

    public void setTrackingId(String trackingId) {
        this.trackingId = trackingId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

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
