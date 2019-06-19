package com.transformedge.teewb.processor.generateewb;

import com.transformedge.teewb.UUIDGenerator;
import com.transformedge.teewb.config.TableMetadataConfiguration;
import com.transformedge.teewb.request.CsvOutputData;
import com.transformedge.teewb.request.Invoice;
import com.transformedge.teewb.request.Item;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.collections.map.HashedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class GenerateEwayBillProcessor implements Processor {
    private static DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    @Autowired
    private TableMetadataConfiguration tableMetadataConfiguration;

    @Autowired
    private UUIDGenerator uuidGenerator;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void process(Exchange exchange) throws Exception {

        List<Map<String, Object>> received = exchange.getIn().getBody(List.class);
        received = convertToLowerCase(received);
        TableMetadataConfiguration.Table invoiceTable = tableMetadataConfiguration.findByName("invoiceTable");
        TableMetadataConfiguration.Table itemTable = tableMetadataConfiguration.findByName("itemTable");
       
        List<String> invoiceColumns = invoiceTable.getColumns();
        List<String> itemColumns = itemTable.getColumns();
        String invoiceIdentifierColumn = invoiceTable.getIdentifierColumn();
        HashMap<String, Invoice> invoicesMap = new HashMap<>();
        
        System.out.println("invoiceTable ::::::"+invoiceTable.getColumns());
        System.out.println("received ::::::"+received);

        for (Map<String, Object> dbRow : received) {
            Map<String, Object> itemRow = new HashMap<>(dbRow);
            itemRow.keySet().retainAll(itemColumns);
            Invoice invoice = invoicesMap.getOrDefault(dbRow.get(invoiceIdentifierColumn), null);
            if (invoice != null) {
                invoice.addItem(new Item(itemRow));
            } else {
                Map<String, Object> invoiceRow = new HashMap<>(dbRow);
                invoiceRow.keySet().retainAll(invoiceColumns);
                invoice = new Invoice(invoiceRow);
                invoice.addItem(new Item(itemRow));
                invoicesMap.put(dbRow.get(invoiceIdentifierColumn).toString(), invoice);
            }
        }
        Collection<Invoice> invoices = invoicesMap.values();
        ArrayList<String> invoiceIdsToUpdate = new ArrayList<>(invoices.size());
        for (Invoice invoice : invoices) {
            invoiceIdsToUpdate.add((String) invoice.getFieldValue(invoiceTable.getIdentifierColumn()));
        }
        exchange.setProperty("listOfInvoicesToBeUpdated", invoiceIdsToUpdate);
        exchange.setProperty("uuid", uuidGenerator.getUuid());

        System.out.println("invoice for testing ::"+invoices);
        exchange.getOut().setBody(invoices);
    }

    private List<Map<String, Object>> convertToLowerCase(List<Map<String, Object>> input) {
        List<Map<String, Object>> newList = new ArrayList<>(input.size());
        for (Map<String, Object> row : input) {
            /*Map<String, Object> newMap = row.keySet().stream()
                                            .collect(Collectors.toMap(key -> key.toLowerCase(), key -> row.get(key)));
            newList.add(newMap);*/
            Map<String, Object> newMap = new HashMap<>();
            Iterator<Map.Entry<String, Object>> entryIterator =  row.entrySet().iterator();
            while (entryIterator.hasNext()) {
                Map.Entry<String, Object> entry = entryIterator.next();
                newMap.put(entry.getKey().toLowerCase(), entry.getValue());
            }
            newList.add(newMap);
        }
        return newList;
    }
}
