package com.transformedge.teewb.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Created by chandrasekarramaswamy on 10/06/18.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionAudit {
    @JsonProperty("_id")
    private String id;
    private String trxId;
    private String type;
    @JsonProperty("_rev")
    private String rev;

    public String getRev() {
        return rev;
    }

    public void setRev(String rev) {
        this.rev = rev;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTrxId() {
        return trxId;
    }

    public void setTrxId(String trxId) {
        this.trxId = trxId;
    }

    public String toJSON() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        try {
            return (mapper.writeValueAsString(this));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return "TransactionAudit{" +
                "id='" + id + '\'' +
                ", trxId=" + trxId +
                ", type='" + type + '\'' +
                '}';
    }

    public static void main(String args[]) {
        TransactionAudit invoiceAudit = new TransactionAudit();
        invoiceAudit.setTrxId("100363");
        invoiceAudit.setType("transaction");
        System.out.println(invoiceAudit.toJSON());
    }
}
