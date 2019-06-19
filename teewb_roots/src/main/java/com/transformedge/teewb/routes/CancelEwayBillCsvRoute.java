package com.transformedge.teewb.routes;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.transformedge.teewb.config.ApiConfiguration;
import com.transformedge.teewb.config.AuditConfiguration;
import com.transformedge.teewb.config.CsvConfiguration;
import com.transformedge.teewb.config.TemplateConfiguration;
import com.transformedge.teewb.entity.TransactionAudit;
import com.transformedge.teewb.processor.EwayBillProcessFailure;
import com.transformedge.teewb.processor.ProcessExceptionCodes;
import com.transformedge.teewb.processor.cancelewb.CancelCsvDataProcessor;
import com.transformedge.teewb.processor.cancelewb.CancelResponseProcessor;
import com.transformedge.teewb.processor.generateewb.PrepareAuditDataProcessor;
import com.transformedge.teewb.request.CancelCsvOutputData;
import com.transformedge.teewb.request.CancelOutputResponse;
import com.transformedge.teewb.request.Invoice;
import com.transformedge.teewb.service.AuditService;

@Component
public class CancelEwayBillCsvRoute extends RouteBuilder {

    @Autowired
    private CancelCsvDataProcessor csvDataProcessor;
    @Autowired
    private CsvConfiguration csvConfiguration;
    @Autowired
    private TemplateConfiguration templateConfiguration;
    @Autowired
    private ApiConfiguration apiConfiguration;
    @Autowired
    private PrepareAuditDataProcessor prepareAuditDataProcessor;
    @Autowired
    private AuditConfiguration auditConfiguration;
    @Autowired
    private CancelResponseProcessor cancelResponseProcessor;
    @Autowired
    private AuditService auditService;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void configure() throws Exception {
        CsvConfiguration.CsvConfig csvConfig = csvConfiguration.findByName("cancelEwayBill");
        CsvDataFormat csvDataFormat = new CsvDataFormat("|");
        csvDataFormat.setQuoteMode(QuoteMode.ALL.toString());

        from("file://" + csvConfig.getInputPath() + "?initialDelay=" + csvConfig.getInitialDelay()+"&delay="+csvConfig.getConsumeDelay()+"&move=.done")
                .routeId("CancelEwayBillCsvPoller")
                //.noAutoStartup()
                .setProperty("origFileName", simple("${header.CamelFileNameOnly}"))
                .log(LoggingLevel.INFO, "Processing CSV file ${header.CamelFileNameOnly} for cancellation request")
                .unmarshal(csvDataFormat)
                .process(csvDataProcessor)
                .split(body()).streaming()
                    .setProperty("origInvoice", simple("${body}"))
                    .to("direct:persistOrFetchTransactionDetails") // this is common route
                    .log(LoggingLevel.INFO, "Cancel EwayBill :: Invoice persisted. Tracking id: ${exchangeProperty.trackingId}")
                    .setBody(simple("${exchangeProperty.origInvoice}"))
                    .to("direct:CsvGenerateEwayBillRequestForCancellation")
                .end()
                .to("direct:CsvCreateCancellationOutputFile");

        from("direct:persistOrFetchTransactionDetails")
                .routeId("persistOrFetchTransactionDetails")
                .process(exchange -> {
                    Invoice origInvoice = exchange.getIn().getBody(Invoice.class);
                    String trxId = String.valueOf(origInvoice.getFieldValue("e_trx_id"));
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

        from("direct:CsvCreateCancellationOutputFile")
                .process(exchange -> {
                    CancelCsvOutputData csvOutputData = exchange.getProperty("outputCSVData", CancelCsvOutputData.class);
                    logger.info("Creating output csv for data: " + csvOutputData);
                    exchange.getOut().setBody(csvOutputData.asList());
                })
                .marshal(csvDataFormat)
                .to("file://" + csvConfig.getOutputPath() + "?fileName=${exchangeProperty.origFileName}")
                .end();

        from("direct:CsvGenerateEwayBillRequestForCancellation")
                .routeId("CsvGenerateEwayBillRequestForCancellation")
                .to("velocity:file://" + templateConfiguration.getTemplatePath() + "CancelEwayBill.vm")
                .setProperty("requestJSON", simple("${body}", String.class))
                .log(LoggingLevel.INFO, "Cancel EwayBill request JSON is: ${body}")
                .to("direct:CsvInvokeCancelEwayBill")
                .end();

        ApiConfiguration.Api api = apiConfiguration.findByName("cancelEwayBill");
        JacksonDataFormat format = new JacksonDataFormat();
        format.setUnmarshalType(CancelOutputResponse.class);
        format.enableFeature(DeserializationFeature.UNWRAP_ROOT_VALUE);
        from("direct:CsvInvokeCancelEwayBill")
                .routeId("CsvInvokeCancelEwayBill")
                .process(exchange -> {
                	 Invoice processedInvoice = exchange.getProperty("origInvoice", Invoice.class);
                 	 String location = (String) processedInvoice.getFieldValue("e_location");
                     String fromGstin = (String) processedInvoice.getFieldValue("e_fromgstin");
                     api.getHeaders().put("Gstin", fromGstin);
                     api.getHeaders().put("Location", location);
//                     GstinHeaders header = api.getHeaders().stream().filter(h -> h.getHeaderGstinName().equals(processedInvoice.getFieldValue("e_fromgstin"))).findAny().get();
//                     Map<String, Object> newMap = header.getHeaderForGstin();
                     Message in = exchange.getIn();
                     Map<String, Object> newMap = api.getHeaders().entrySet().stream()
                                                     .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue()));
                  
                     System.out.println("newMap :::"+newMap);
                     in.setHeaders(newMap);
                })
                .doTry()
                    .to(api.getUrl())
                    .unmarshal(format)
                    .log(LoggingLevel.INFO, "Response from CancelEwayBill API is: ${body}")
                    .to("direct:CsvProcessCancelEwayBillResponse")
                .doCatch(Exception.class)
                    .log(LoggingLevel.ERROR, "Exception occured when invoking Cancel Eway Bill APIs ${exchangeProperty.CamelExceptionCaught}")
                    .process(new Processor() {
                        @Override
                        public void process(Exchange exchange) throws Exception {
                            Throwable cause = exchange.getProperty("CamelExceptionCaught", Throwable.class);
                            exchange.setProperty("EwayBillException", new EwayBillProcessFailure(ProcessExceptionCodes.SYSTEM_ERROR, cause));
                        }
                    })
                .endDoTry();

        from("direct:CsvProcessCancelEwayBillResponse")
                .routeId("CsvProcessCancelEwayBillResponse")
                .setProperty("origResponse", simple("${body}"))
                .setProperty("responseJSON", simple("${body.toJSON()}"))
                .to("direct:CsvPersistAuditForCancellation")
                .setBody(simple("${exchangeProperty.origResponse}"))
                .removeProperty("origResponse")
                .process(cancelResponseProcessor);

        from("direct:CsvPersistAuditForCancellation")
                .routeId("CsvPersistAuditForCancellation")
                .marshal().json(JsonLibrary.Jackson)
                .process(prepareAuditDataProcessor)
                .to("couchdb:" + auditConfiguration.getUrl());
    }
}
