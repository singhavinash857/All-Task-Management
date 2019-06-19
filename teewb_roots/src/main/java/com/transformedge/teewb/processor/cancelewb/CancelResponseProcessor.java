package com.transformedge.teewb.processor.cancelewb;


import com.transformedge.teewb.request.CancelCsvOutputData;
import com.transformedge.teewb.request.CancelOutputResponse;
import com.transformedge.teewb.request.Invoice;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CancelResponseProcessor implements Processor {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void process(Exchange exchange) throws Exception {
        CancelOutputResponse cancelOutputResponse = exchange.getIn().getBody(CancelOutputResponse.class);
        Invoice processedInvoice = exchange.getProperty("origInvoice", Invoice.class);
        CancelCsvOutputData csvOutputData = exchange.getProperty("outputCSVData", CancelCsvOutputData.class);

        logger.info("Processing cancel response for invoice: " + processedInvoice.getFieldValue("e_trx_id"));
        CancelCsvOutputData.CsvOutputRow row = csvOutputData.newRow();
        if (cancelOutputResponse.isSuccess()) {
            row.addColumn("e_trx_id", processedInvoice.getFieldValue("e_trx_id"));
            row.addColumn("intg_tracking_id", exchange.getIn().getHeader("CouchDbId"));
            row.addColumn("e_process_source", processedInvoice.getFieldValue("e_process_source"));
            row.addColumn("e_process_operation", processedInvoice.getFieldValue("e_process_operation"));
            row.addColumn("e_doc_num", processedInvoice.getFieldValue("e_doc_num"));
            row.addColumn("e_doc_type", processedInvoice.getFieldValue("e_doc_type"));
            row.addColumn("g_ewayBillNo", cancelOutputResponse.getColumnValue("ewayBillNo"));
            row.addColumn("g_ewbCancelDate", cancelOutputResponse.getColumnValue("cancelDate"));
            row.addColumn("e_process_status", "P");
            row.addColumn("g_error_code", "");
            row.addColumn("g_error", "");
            row.addColumn("g_error_description", "");
        } else {
            row.addColumn("e_trx_id", processedInvoice.getFieldValue("e_trx_id"));
            row.addColumn("intg_tracking_id", exchange.getIn().getHeader("CouchDbId"));
            row.addColumn("e_process_source", processedInvoice.getFieldValue("e_process_source"));
            row.addColumn("e_process_operation", processedInvoice.getFieldValue("e_process_operation"));
            row.addColumn("e_doc_num", processedInvoice.getFieldValue("e_doc_num"));
            row.addColumn("e_doc_type", processedInvoice.getFieldValue("e_doc_type"));
            row.addColumn("g_ewayBillNo", "");
            row.addColumn("g_ewbCancelDate", "");
            row.addColumn("e_process_status", "E");
            row.addColumn("g_error_code", cancelOutputResponse.getColumnValue("errorCodes"));
            row.addColumn("g_error", cancelOutputResponse.getColumnValue("errorDescription"));
            row.addColumn("g_error_description", cancelOutputResponse.getColumnValue("errorDescription"));
        }
        logger.info("csv row created is: " + row);
        csvOutputData.addRow(row);
    }
}
