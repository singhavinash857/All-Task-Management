package com.transformedge.teewb.processor.updatevehicle;

import com.transformedge.teewb.config.CsvConfiguration;
import com.transformedge.teewb.config.TableMetadataConfiguration;
import com.transformedge.teewb.request.CancelCsvOutputData;
import com.transformedge.teewb.request.Invoice;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chandrasekarramaswamy on 18/07/18.
 */
@Component
public class UpdateVehicleCsvRequestProcessor implements Processor {
    @Autowired
    private CsvConfiguration csvConfiguration;

    @Override
    public void process(Exchange exchange) throws Exception {
        List<List<String>> csvData = (List<List<String>>) exchange.getIn().getBody();
        CsvConfiguration.CsvConfig csvConfig = csvConfiguration.findByName("updateVehicle");
        TableMetadataConfiguration.Table updateEwayBillTable = csvConfig.getTableMetadataConfiguration().findByName("invoiceTable");
        List<String> inputColumns = updateEwayBillTable.getColumns();
        List<Invoice> invoicesList = new ArrayList<>();
        
        System.out.println("inputColumns in update :::"+inputColumns);
        System.out.println("csvData in update :::"+csvData);
        for (List<String> row : csvData) {
            Invoice invoice = new Invoice();
            for (int i=0;i<row.size();i++) {
                invoice.addField(inputColumns.get(i), row.get(i));
            }
            invoicesList.add(invoice);
        }
        exchange.setProperty("outputCSVData", new CancelCsvOutputData());
        exchange.getOut().setBody(invoicesList);
    }
}
