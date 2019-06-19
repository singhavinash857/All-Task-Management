package com.transformedge.teewb.processor.generateewb;

import com.transformedge.teewb.config.CsvConfiguration;
import com.transformedge.teewb.config.TableMetadataConfiguration;
import com.transformedge.teewb.processor.EwayBillProcessFailure;
import com.transformedge.teewb.processor.ProcessExceptionCodes;
import com.transformedge.teewb.request.CsvOutputData;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;
import java.util.regex.Pattern;

@Component
public class CsvDataProcessor implements Processor {

    @Autowired
    private CsvConfiguration csvConfiguration;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final String regexPattern = "^([0-9]+\\.?[0-9]*|[0-9]*\\.[0-9]+)$";

    @Override
    public void process(Exchange exchange) throws Exception {

        CsvConfiguration.CsvConfig csvConfig = csvConfiguration.findByName("generateEwbForInvoiceDocuments");

        TableMetadataConfiguration.Table table = csvConfig.getTableMetadataConfiguration().findByName("invoiceTable");

        List<List<String>> csvData = (List<List<String>>) exchange.getIn().getBody();
        logger.debug("CSV data read from file: " + exchange.getProperty("origFileName").toString() + " is: ");
        logger.debug(csvData.toString());
        List<String> invoiceTableColumns = new ArrayList(table.getColumns());
        TableMetadataConfiguration.Table itemTable = csvConfig.getTableMetadataConfiguration().findByName("itemTable");
        List<String> itemTableColumns = new ArrayList(itemTable.getColumns());

        invoiceTableColumns.addAll(itemTableColumns);

        List<Map<String, Object>> data = new ArrayList<Map<String, Object>>(csvData.size());

        List<String> numericColumns = csvConfig.getValidation().getNumericColumns();

        List<String> invalidTrxRecords = new ArrayList<>();

        for (List<String> values : csvData) {
            Map<String, Object> row = new LinkedHashMap<>();
            for (int i = 0; i<invoiceTableColumns.size(); i++) {

                String key = invoiceTableColumns.get(i);
                if (numericColumns.contains(key)) {
                    try {
                        row.put(key, NumberFormat.getInstance().parse(values.get(i)));
                    } catch (ParseException e) {
                        logger.error("Invalid data format for field: " + key + ". It should be an integer");
                    }
                } else {
                    row.put(key, values.get(i));
                }
            }
            data.add(row);
        }

        CsvOutputData csvOutputData = new CsvOutputData(data);
        exchange.setProperty("outputCSVData", csvOutputData);
        exchange.getOut().setBody(data);
    }
}
