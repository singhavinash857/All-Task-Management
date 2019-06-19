package com.transformedge.teewb.processor.generateewb;

import com.transformedge.teewb.config.CsvConfiguration;
import com.transformedge.teewb.config.TableMetadataConfiguration;
import com.transformedge.teewb.processor.ProcessExceptionCodes;
import com.transformedge.teewb.request.CsvFailedValidationOutput;
import com.transformedge.teewb.request.Invoice;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CsvValidationProcessor implements Processor {

    @Autowired
    private CsvConfiguration csvConfiguration;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String regexPattern = "^([0-9]+\\.?[0-9]*|[0-9]*\\.[0-9]+)$";

    @Override
    public void process(Exchange exchange) throws Exception {

        CsvConfiguration.CsvConfig csvConfig = csvConfiguration.findByName("generateEwbForInvoiceDocuments");

        TableMetadataConfiguration.Table table = csvConfig.getTableMetadataConfiguration().findByName("invoiceTable");
        List<List<String>> csvData = (List<List<String>>) exchange.getIn().getBody();
        logger.info("csvData :"+csvData);
        List<List<String>> filteredCsvData = new ArrayList<>();
        List<String> numericColumns = csvConfig.getValidation().getNumericColumns();
        List<String> invoiceTableColumns = new ArrayList(table.getColumns());
        TableMetadataConfiguration.Table itemTable = csvConfig.getTableMetadataConfiguration().findByName("itemTable");
        List<String> itemTableColumns = new ArrayList(itemTable.getColumns());
        invoiceTableColumns.addAll(itemTableColumns);
        List<CsvFailedValidationOutput> failedValidationOutputs = new ArrayList<>();

        outer: for (List<String> values : csvData) {
            boolean isValid = true;
            for (CsvFailedValidationOutput output : failedValidationOutputs) {
                if (output.hasInvoice(values.get(0), table.getIdentifierColumn())) {
                    continue outer;
                }

            }
            for (int i = 0; i<invoiceTableColumns.size(); i++) {
                String key = invoiceTableColumns.get(i);
                if (numericColumns.contains(key)) {
                    if (!values.get(i).matches(regexPattern)) {
                        logger.info("Invoice: " + values.get(0) + " has invalid data for field: " + key);
                        Invoice invoice = new Invoice();
                        invoice.addField(table.getIdentifierColumn(), values.get(0));
                        CsvFailedValidationOutput csvFailedValidationOutput = new CsvFailedValidationOutput(invoice,
                                                    "Field: " + key + " should be a number but has invalid value: " + values.get(i),
                                                    ProcessExceptionCodes.VALIDATION_ERROR);
                        isValid = false;
                        failedValidationOutputs.add(csvFailedValidationOutput);
                    }
                }
            }
            if (isValid)
                filteredCsvData.add(values);
        }
        exchange.setProperty("validationFailedInvoices", failedValidationOutputs);
        exchange.getOut().setBody(filteredCsvData);
    }

}
