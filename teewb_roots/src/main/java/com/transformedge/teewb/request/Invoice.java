package com.transformedge.teewb.request;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class Invoice extends AbstractRequest {
    private List<Item> items;

    public Invoice() {
        this.items = new ArrayList<>();
    }

    public Invoice(Map<String, Object> fields) {
        this();
        this.setFields(fields);
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public void addItem(Item item) {
        items.add(item);
    }

    @JsonIgnore
    public boolean isNew() {
        return (this.getFieldValue("e_process_status").equals("N"));
    }

    @Override
    public String toString() {
        return "[Invoice: " + getFields().toString() + "]. Items: [" + getItems().toString() + "]";
    }

    public String toJSON() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static void main(String args[])  {
        Invoice invoice = new Invoice();
        invoice.addField("trx_id", "100001");
        invoice.addField("docno", "ABC5000");
        Item item = new Item();
        item.addField("hsncode", "3000");
        item.addField("cessvalue", 0.00);
        invoice.addItem(item);
        System.out.println(invoice.toJSON());
    }
}
