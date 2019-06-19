package com.transformedge.teewb.request;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.model.dataformat.CsvDataFormat;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.commons.csv.QuoteMode;
import org.apache.velocity.tools.generic.DateTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.transformedge.teewb.config.ApiConfiguration;
import com.transformedge.teewb.config.AuditConfiguration;
import com.transformedge.teewb.config.CsvConfiguration;
import com.transformedge.teewb.config.QueryConfiguration;
import com.transformedge.teewb.config.TableMetadataConfiguration;
import com.transformedge.teewb.config.TemplateConfiguration;
import com.transformedge.teewb.entity.TransactionAudit;
import com.transformedge.teewb.processor.EwayBillProcessFailure;
import com.transformedge.teewb.processor.ProcessExceptionCodes;
import com.transformedge.teewb.processor.generateewb.CsvDataProcessor;
import com.transformedge.teewb.processor.generateewb.CsvValidationProcessor;
import com.transformedge.teewb.processor.generateewb.GenerateCsvOutputProcessor;
import com.transformedge.teewb.processor.generateewb.GenerateEwayBillProcessor;
import com.transformedge.teewb.processor.generateewb.PrepareAuditDataProcessor;
import com.transformedge.teewb.service.AuditService;

@Component
public class GenerateEwayBillForCsvRoute extends RouteBuilder {

    @Autowired
    private QueryConfiguration queryConfiguration;

    @Autowired
    private CsvDataProcessor csvDataProcessor;
    @Autowired
    private GenerateEwayBillProcessor generateEwayBillProcessor;
    @Autowired
    private TableMetadataConfiguration tableMetadataConfiguration;

    @Autowired
    private TemplateConfiguration templateConfiguration;

    @Autowired
    private ApiConfiguration apiConfiguration;

    @Autowired
    private AuditConfiguration auditConfiguration;

    @Autowired
    private PrepareAuditDataProcessor prepareAuditDataProcessor;

    @Autowired
    private GenerateCsvOutputProcessor generateCsvOutputProcessor;

    @Autowired
    private CsvValidationProcessor csvValidationProcessor;

    @Autowired
    private CsvConfiguration csvConfiguration;

    @Autowired
    private AuditService auditService;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final String regexPattern = "^([0-9]+\\.?[0-9]*|[0-9]*\\.[0-9]+)$";

    @Override
    public void configure() throws Exception {

        CsvConfiguration.CsvConfig csvConfig = csvConfiguration.findByName("generateEwbForInvoiceDocuments");
        TableMetadataConfiguration.Table table = csvConfig.getTableMetadataConfiguration().findByName("invoiceTable");
        onException(IllegalStateException.class, IOException.class)
                .to("direct:processInvalidFile");
        from("file://" + csvConfig.getInputPath() + "?initialDelay=" + csvConfig.getInitialDelay()+"&delay="+csvConfig.getConsumeDelay()+"&move=.done")
                .routeId("CSVPoller")
                .noAutoStartup()
                .log(LoggingLevel.INFO, "Processing CSV file ${header.CamelFileNameOnly}")
                .setProperty("origFileName", simple("${header.CamelFileNameOnly}"))
                .unmarshal(new CsvDataFormat("|"))
                .process(csvValidationProcessor)
                .log("CSV data after validation: ${body}")
                .log("validation failed invoices: ${exchangeProperty.validationFailedInvoices}")
                .process(csvDataProcessor)
                .process(generateEwayBillProcessor)
                .split(body()).streaming()
                    .setProperty("origInvoice", simple("${body}"))
                    .to("direct:persistRequestInvoiceForCsv")
                    .setBody(simple("${exchangeProperty.origInvoice}"))
                    .to("direct:prepareRequestJSONForCsv")
                .end()
                .choice()
                    .when(simple("${exchangeProperty.validationFailedInvoices} != null"))
                        .log("Processing validation failed invoices")
                        .setBody(simple("${exchangeProperty.validationFailedInvoices}"))
                        .split(body()).streaming()
                                    .setProperty("csvFailedValidationOutput", simple("${body}"))
                                    .setProperty("origInvoice", simple("${body.getFailedInvoice}"))
                                    .setBody(simple("${exchangeProperty.origInvoice}"))
                                    .to("direct:persistRequestInvoiceForCsv")
                                    .setBody(simple("${exchangeProperty.csvFailedValidationOutput}"))
                                    .process(exchange -> {
                                        CsvOutputData csvOutputData = exchange.getProperty("outputCSVData", CsvOutputData.class);
                                        logger.debug("Mudiyala processor:: " + exchange.getIn().getBody().getClass().getName());
                                        CsvFailedValidationOutput validationOutput = exchange.getIn().getBody(CsvFailedValidationOutput.class);
                                        Invoice failedInvoice = validationOutput.getFailedInvoice();
                                        String processedInvoiceID = (String) failedInvoice.getFieldValue(table.getIdentifierColumn());
                                        csvOutputData.addData(failedInvoice.getFields());
                                        csvOutputData.addDataToRows(table.getIdentifierColumn(), processedInvoiceID, "e_process_status", "E");
                                        csvOutputData.addDataToRows(table.getIdentifierColumn(), processedInvoiceID,
                                                "g_ewaybill_no", "");
                                        csvOutputData.addDataToRows(table.getIdentifierColumn(), processedInvoiceID,
                                                "g_valid_upto", "");
                                        csvOutputData.addDataToRows(table.getIdentifierColumn(), processedInvoiceID,
                                                "g_error", validationOutput.getErrorMessage());
                                        csvOutputData.addDataToRows(table.getIdentifierColumn(), processedInvoiceID,
                                                "g_error_code", validationOutput.getProcessExceptionCodes().getErrorCode());
                                        csvOutputData.addDataToRows(table.getIdentifierColumn(), processedInvoiceID,
                                                "g_error_description", validationOutput.getErrorMessage());


                                        EwayBillProcessFailure exception = new EwayBillProcessFailure(validationOutput.getProcessExceptionCodes(),
                                                                                    validationOutput.getErrorMessage());
                                        exchange.setProperty("EwayBillException", exception);
                                    })
                                    .to("direct:processExceptionResponseForCsv")
                        .end() // end of split body
                    .end() // end of when ${exchangeProperty.validationFailedInvoices} != null
                .to("direct:prepareOutputForCsv")
                .to("direct:createCsvOutput");

        CsvDataFormat csvDataFormat = new CsvDataFormat("|");
        csvDataFormat.setQuoteMode(QuoteMode.ALL.toString());
        csvDataFormat.setIgnoreEmptyLines(true);
        from("direct:prepareOutputForCsv")
                .log(LoggingLevel.INFO, "Creating output CSV file: ${exchangeProperty.origFileName}")
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        CsvOutputData csvOutputData = exchange.getProperty("outputCSVData", CsvOutputData.class);
                        exchange.getOut().setBody(csvOutputData.filterColumnsAndGetData(table.getIdentifierColumn(),
                                                                                        csvConfig.getOutputColumns()));
                    }
                });
        from("direct:processInvalidFile")
                .log("The CSV file is invalid. Please check the data")
                .process(exchange -> {
                    Throwable throwable = exchange.getProperty("CamelExceptionCaught", Throwable.class);
                    List<String> outputColuimns = csvConfig.getOutputColumns();
                    List<Map<String,String>> data = new ArrayList<Map<String, String>>(1);
                    Map<String, String> row = new LinkedHashMap<String, String>();
                    for (String columnName: outputColuimns) {
                        switch (columnName)  {
                            case "e_process_status" :
                                row.put(columnName, "E");
                                break;
                            case "g_error_code" :
                                row.put(columnName, ProcessExceptionCodes.INVALID_FILE_CONTENT.getErrorCode());
                                break;
                            case "g_error" :
                                row.put(columnName, throwable.getMessage());
                                break;
                            case "g_error_description" :
                                row.put(columnName, throwable.getMessage());
                                break;
                            default:
                                row.put(columnName, "");
                        }
                        data.add(row);
                    }
                    exchange.getOut().setBody(row);
                })
                .to("direct:createCsvOutput");

        from("direct:createCsvOutput")
                .marshal(csvDataFormat)
                .to("file://" + csvConfig.getOutputPath() + "?fileName=${exchangeProperty.origFileName}")
                .end();

        from("direct:persistRequestInvoiceForCsv")
                .routeId("PersistRequestInvoiceForCsv")
                .process(exchange -> {
                    Invoice origInvoice = exchange.getIn().getBody(Invoice.class);
                    String trxId = String.valueOf(origInvoice.getFieldValue(table.getIdentifierColumn()));
                    TransactionAudit existingTx = auditService.findTransactionAudit(trxId);
                    if (existingTx == null) {
                        logger.info("Invoice with number: " + trxId + " doesn't exist in database. Creating new Invoice record");
                        TransactionAudit invoiceAudit = new TransactionAudit();
                        invoiceAudit.setType("transaction");
                        invoiceAudit.setTrxId(trxId);
                        exchange.setProperty("isNew", true);
                        exchange.getOut().setBody(invoiceAudit.toJSON());
                    } else {
                        logger.info("Invoice with number: " + trxId + " already exists in database");
                        exchange.setProperty("trackingId", existingTx.getId());
                    }
                })
                .choice()
                    .when(simple("${exchangeProperty.isNew} == true"))
                        .to("couchdb:" + auditConfiguration.getUrl())
                        .setProperty("trackingId", simple("${header.CouchDbId}"))
                    .otherwise()
                        .log("Existing invoice. No need to persist in Database!!!")
                    .end();
                //.to("direct:prepareRequestJSONForCsv");

        from("direct:prepareRequestJSONForCsv")
                .routeId("PrepareRequestJSONRouteForCsv")
                .tracing().log("Processing invoices : ${body}")
                .setProperty("formatter").constant(new DateTool())
                .to("velocity:file://" + templateConfiguration.getTemplatePath() + "GenerateEwayBillPerInvoice.vm")
                .setProperty("requestJSON", simple("${body}", String.class))
                .log(LoggingLevel.INFO, "Generate EwayBill request JSON is: ${body}")

                .to("direct:callEwayBillAPIForCsv");

        ApiConfiguration.Api api = apiConfiguration.findByName("generateEwayBill");
        
        JacksonDataFormat format = new JacksonDataFormat();
        format.setUnmarshalType(OutputResponse.class);
        format.enableFeature(DeserializationFeature.UNWRAP_ROOT_VALUE);
        
        from("direct:callEwayBillAPIForCsv")
                .routeId("InvokeEwayBillApiRouteForCsv")
                .process(exchange -> {
                	
                    Invoice processedInvoice = exchange.getProperty("origInvoice", Invoice.class);
                   
                    String location = (String) processedInvoice.getFieldValue("e_location");
                    String fromGstin = (String) processedInvoice.getFieldValue("e_fromgstin");
//                  GstinHeaders header = api.getHeaders().stream().filter(h -> h.getHeaderGstinName().equals(processedInvoice.getFieldValue("e_fromgstin"))).findAny().get();
//                  Map<String, Object> newMap = header.getHeaderForGstin();
                    Message in = exchange.getIn();
                    api.getHeaders().put("Gstin", fromGstin);
                    api.getHeaders().put("Location", location);

                    Map<String, Object> newMap = api.getHeaders().entrySet().stream()
                                                    .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue()));
                    in.setHeaders(newMap);
                })
                .doTry()
                    .to(api.getUrl())
                    .unmarshal(format)
                    .log(LoggingLevel.INFO, "Response from GenerateEwayBill API is: ${body.toJSON()}")
                    .to("direct:processResponseForCsv")
                .doCatch(Exception.class)
                    .log(LoggingLevel.ERROR, "Exception occured when invoking Eway Bill APIs ${exchangeProperty.CamelExceptionCaught}")
                    .process(new Processor() {
                        @Override
                        public void process(Exchange exchange) throws Exception {
                            Throwable cause = exchange.getProperty("CamelExceptionCaught", Throwable.class);
                            exchange.setProperty("EwayBillException", new EwayBillProcessFailure(ProcessExceptionCodes.SYSTEM_ERROR, cause));
                        }
                    })
                    .to("direct:processExceptionResponseForCsv")
                .endDoTry();

        from("direct:processExceptionResponseForCsv")
                .routeId("ProcessErrorResponseForCsv")
                //.process(generateCsvOutputProcessor)
                .bean(OutputResponse.class, "fromValidationException(${exchangeProperty.EwayBillException})")
                .process(generateCsvOutputProcessor)
                .setProperty("responseJSON", simple("${body.toJSON()}"))
                .to("direct:persistAuditForCsv");

        from("direct:processResponseForCsv")
                .routeId("PrepareAuditDataRouteForCsv")
                .setProperty("responseJSON", simple("${body.toJSON()}"))
                .process(generateCsvOutputProcessor)
                .log(LoggingLevel.INFO, "Preparing CSV response for file ${exchangeProperty.origFileName}")
                //.end()
                .to("direct:persistAuditForCsv");

        from("direct:persistAuditForCsv")
                .routeId("PersistAuditDataRouteForCsv")
                .marshal().json(JsonLibrary.Jackson)
                .process(prepareAuditDataProcessor)
                .to("couchdb:" + auditConfiguration.getUrl())
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        Invoice processedInvoice = exchange.getProperty("origInvoice", Invoice.class);
                        CsvOutputData csvOutputData = (CsvOutputData) exchange.getProperty("outputCSVData");
                        csvOutputData.addDataToRows(table.getIdentifierColumn(),
                                                    (String) processedInvoice.getFieldValue(table.getIdentifierColumn()),
                                                    "intg_tracking_id", exchange.getIn().getHeader("CouchDbId"));
                   
                    }
                })
                .end()
                .log(LoggingLevel.INFO, "Generate Eway Bill process completed !!!");
    }

}
