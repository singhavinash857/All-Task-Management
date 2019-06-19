package com.transformedge.teewb.processor.generateewb;

import com.transformedge.teewb.config.CsvConfiguration;
import com.transformedge.teewb.config.TableMetadataConfiguration;
import com.transformedge.teewb.request.CsvOutputData;
import com.transformedge.teewb.request.Invoice;
import com.transformedge.teewb.request.Item;
import com.transformedge.teewb.request.OutputResponse;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GenerateCsvOutputProcessor implements Processor {

    @Autowired
    private CsvConfiguration csvConfiguration;

    @Override
    public void process(Exchange exchange) throws Exception {
        TableMetadataConfiguration.Table invoiceTable = csvConfiguration.findByName("generateEwbForInvoiceDocuments")
                                                            .getTableMetadataConfiguration().findByName("invoiceTable");

        CsvOutputData csvOutputData = (CsvOutputData) exchange.getProperty("outputCSVData");
        Invoice processedInvoice = exchange.getProperty("origInvoice", Invoice.class);

        String processedInvoiceID = String.valueOf(processedInvoice.getFieldValue(invoiceTable.getIdentifierColumn()));

        OutputResponse outputResponse = exchange.getIn().getBody(OutputResponse.class);

        csvOutputData.addDataToRows(invoiceTable.getIdentifierColumn(), processedInvoiceID,
                                    "g_status", format(outputResponse.getStatus()));
        if (outputResponse.isSuccess()) {
            csvOutputData.addDataToRows(invoiceTable.getIdentifierColumn(), processedInvoiceID,
                                        "e_doctype", processedInvoice.getFieldValue("e_doctype"));
            csvOutputData.addDataToRows(invoiceTable.getIdentifierColumn(), processedInvoiceID,
                                        "e_docno", processedInvoice.getFieldValue("e_docno"));
            csvOutputData.addDataToRows(invoiceTable.getIdentifierColumn(), processedInvoiceID, "e_process_status", "P");
            csvOutputData.addDataToRows(invoiceTable.getIdentifierColumn(), processedInvoiceID,
                                        "g_ewaybill_no", format(outputResponse.getEwayBillNo()));
            csvOutputData.addDataToRows(invoiceTable.getIdentifierColumn(), processedInvoiceID,
                                        "g_ewaybill_date", format(outputResponse.getEwayBillDate()));
            csvOutputData.addDataToRows(invoiceTable.getIdentifierColumn(), processedInvoiceID,
                                        "g_valid_upto", format(outputResponse.getValidUpto()));
            csvOutputData.addDataToRows(invoiceTable.getIdentifierColumn(), processedInvoiceID,
                                        "g_error", "");
            csvOutputData.addDataToRows(invoiceTable.getIdentifierColumn(), processedInvoiceID,
                                        "g_error_code", "");
            csvOutputData.addDataToRows(invoiceTable.getIdentifierColumn(), processedInvoiceID,
                                        "g_error_description", "");
        } else {
            csvOutputData.addDataToRows(invoiceTable.getIdentifierColumn(), processedInvoiceID,
                                        "e_doctype", processedInvoice.getFieldValue("e_doctype"));
            csvOutputData.addDataToRows(invoiceTable.getIdentifierColumn(), processedInvoiceID,
                                        "e_docno", processedInvoice.getFieldValue("e_docno"));
            csvOutputData.addDataToRows(invoiceTable.getIdentifierColumn(), processedInvoiceID, "e_process_status", "E");
            csvOutputData.addDataToRows(invoiceTable.getIdentifierColumn(), processedInvoiceID,
                                        "g_ewaybill_no", "");
            csvOutputData.addDataToRows(invoiceTable.getIdentifierColumn(), processedInvoiceID,
                                        "g_valid_upto", "");
            csvOutputData.addDataToRows(invoiceTable.getIdentifierColumn(), processedInvoiceID,
                                            "g_error", format(outputResponse.getError()));
            csvOutputData.addDataToRows(invoiceTable.getIdentifierColumn(), processedInvoiceID,
                                        "g_error_code", format(outputResponse.getErrorCodes()));
            
            csvOutputData.addDataToRows(invoiceTable.getIdentifierColumn(), processedInvoiceID,
                                        "g_error_description", format(outputResponse.getErrorDesciption()));
        }
    }

    private String format(String input) {
        return input == null || input.equalsIgnoreCase("null") ? "" : input;
    }
}
