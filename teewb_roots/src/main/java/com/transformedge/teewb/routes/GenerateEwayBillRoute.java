package com.transformedge.teewb.routes;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.transformedge.teewb.config.*;
import com.transformedge.teewb.entity.TransactionAudit;
import com.transformedge.teewb.processor.generateewb.GenerateEwayBillProcessor;
import com.transformedge.teewb.processor.generateewb.PrepareAuditDataProcessor;
import com.transformedge.teewb.request.Invoice;
import com.transformedge.teewb.request.OutputResponse;
import com.transformedge.teewb.service.AuditService;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.velocity.tools.generic.DateTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GenerateEwayBillRoute extends RouteBuilder {
    @Autowired
    private GenerateEwayBillProcessor generateEwayBillProcessor;

    @Autowired
    private QueryConfiguration queryConfiguration;

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
    private AuditService auditService;

    private static final String sqlDataSource = "?dataSource=dataSource";

    @Override
    public void configure() throws Exception {
        // actual code below
        QueryConfiguration.Query query = queryConfiguration.findByName("generateEwbForInvoiceDocuments");
        TableMetadataConfiguration.Table table = tableMetadataConfiguration.findByName("invoiceTable");
        String fetchEwbQuery = "sql:" + query.getQuery()
                                + "?dataSource=dataSource&consumer.initialDelay="+query.getInitialDelay()
                                +"&consumer.delay="+query.getConsumeDelay()+"&useIterator=false&outputType=StreamList";

        String timerUrl = "timer:processEwayBills?delay=" + query.getInitialDelay() + "&period=" + query.getConsumeDelay()
                            + "&fixedRate=true";
        from(fetchEwbQuery)
                .routeId("EwayBillProcessRoute")
                .noAutoStartup()
                .process(generateEwayBillProcessor)
                .split(body()).streaming()
                    .to("direct:persistRequestInvoice")
                .end();

        from("direct:persistRequestInvoice")
                .routeId("PersistRequestInvoice")
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        Invoice origInvoice = exchange.getIn().getBody(Invoice.class);
                        exchange.setProperty("origInvoice", origInvoice);
                        String trxId = String.valueOf(origInvoice.getFieldValue(table.getIdentifierColumn()));

                        TransactionAudit existingTx = auditService.findTransactionAudit(trxId);
                        if (existingTx == null) {
                            System.out.println("invoice doesnt exist in route");
                            TransactionAudit invoiceAudit = new TransactionAudit();
                            invoiceAudit.setType("transaction");
                            invoiceAudit.setTrxId(trxId);
                            exchange.setProperty("isNew", true);
                            exchange.getOut().setBody(invoiceAudit.toJSON());
                        } else {
                            exchange.setProperty("trackingId", existingTx.getId());
                        }
                    }
                })
                .choice()
                    .when(simple("${exchangeProperty.isNew} == true"))
                        .log("Invoice doesn't exist. Creating new transaction record")
                        .to("couchdb:" + auditConfiguration.getUrl())
                        .setProperty("trackingId", simple("${header.CouchDbId}"))
                        .setBody(simple("${exchangeProperty.origInvoice}"))
                        .removeProperty("origInvoice")
                    .otherwise()
                        .log("Existing invoice. No need to persist in Database!!!")
                    .end()
                .to("direct:prepareRequestJSON");

        from("direct:prepareRequestJSON")
                .routeId("PrepareRequestJSONRoute")
                .tracing().log("Processing invoice: ${body}")
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        Invoice invoice = exchange.getIn().getBody(Invoice.class);
                        exchange.setProperty("processedInvoiceID", invoice.getFieldValue(table.getIdentifierColumn()));
                    }
                })
                .setProperty("formatter").constant(new DateTool())
                .to("velocity:file://" + templateConfiguration.getTemplatePath() + "GenerateEwayBillPerInvoice.vm")
                .setProperty("requestJSON", simple("${body}", String.class))
                .log("Request JSON is: ${body}")
                .to("direct:callEwayBillAPI");

        ApiConfiguration.Api api = apiConfiguration.findByName("generateEwayBill");
        JacksonDataFormat format = new JacksonDataFormat();
        format.setUnmarshalType(OutputResponse.class);
        format.enableFeature(DeserializationFeature.UNWRAP_ROOT_VALUE);
        from("direct:callEwayBillAPI")
                .routeId("InvokeEwayBillApiRoute")
//                .setHeader("Ocp-Apim-Subscription-Key", constant(api.getHeaderValue("Ocp-Apim-Subscription-Key")))
//                .setHeader("Gstin", constant(api.getHeaderValue("Gstin")))
//                .setHeader("sourcetype", constant(api.getHeaderValue("sourcetype")))
//                .setHeader("referenceno", constant(api.getHeaderValue("referenceno")))
//                .setHeader("outputtype", constant(api.getHeaderValue("outputtype")))
//                .setHeader("Content-Type", constant(api.getHeaderValue("Content-Type")))
//                .setHeader("Location", constant(api.getHeaderValue("Location")))
//                .setHeader(Exchange.HTTP_METHOD, constant(api.getHeaderValue("HTTP_METHOD")))
                .doTry()
                    .to(api.getUrl())
                    .unmarshal(format)
                    .log("Response from API is: ${body}")
                    .to("direct:processResponse")
                .doCatch(Exception.class)
                    .to("direct:processExceptionResponse")
                    .log("Exception occured when invoking Eway Bill APIs")
                .endDoTry();

        from("direct:processExceptionResponse")
                .routeId("ProcessErrorResponse")
                .to("sql:" + query.getOnExceptionInvoiceQuery() + sqlDataSource)
                .bean(OutputResponse.class, "fromException(${exchangeProperty.CamelExceptionCaught})")
                .setProperty("responseJSON", simple("${body.toJSON()}"))
                .to("direct:persistAudit");

        from("direct:processResponse")
                .routeId("PrepareAuditDataRoute")
                .setProperty("responseJSON", simple("${body.toJSON()}"))
                .choice()
                    .when(simple("${body.isSuccess}"))
                        .log("Processing successful response")
                        .to("sql:" + query.getOnConsumeInvoiceQuery() + sqlDataSource)
                    .otherwise()
                        .log("Processing error response")
                        .to("sql:" + query.getOnConsumeFailInvoiceQuery() + sqlDataSource)
                    .end()
                .to("direct:persistAudit");

        from("direct:persistAudit")
                .routeId("PersistAuditDataRoute")
                .marshal().json(JsonLibrary.Jackson)
                .process(prepareAuditDataProcessor)
                .to("couchdb:" + auditConfiguration.getUrl())
                .to("sql:" + query.getUpdateTrackingAndBatchQuery() + sqlDataSource)
                .log("Generate Eway Bill process completed !!!");
    }

}
