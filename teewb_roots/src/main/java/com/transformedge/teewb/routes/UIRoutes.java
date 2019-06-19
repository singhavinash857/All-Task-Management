package com.transformedge.teewb.routes;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.transformedge.teewb.config.QueryConfiguration;
import com.transformedge.teewb.config.TemplateConfiguration;
import com.transformedge.teewb.entity.Audit;
import com.transformedge.teewb.processor.generateewb.GenerateEwayBillProcessor;
import com.transformedge.teewb.processor.reports.GenerateEwayBillPDFProcessor;
import com.transformedge.teewb.request.OutputResponse;
import com.transformedge.teewb.service.AuditService;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.MediaType;

/**
 * Created by chandrasekarramaswamy on 20/05/18.
 */
@Component
public class UIRoutes extends RouteBuilder {

    @Autowired
    private TemplateConfiguration templateConfiguration;

    @Autowired
    private AuditService auditService;

    @Autowired
    private QueryConfiguration queryConfiguration;

    @Autowired
    private GenerateEwayBillProcessor generateEwayBillProcessor;

    @Autowired
    private GenerateEwayBillPDFProcessor generateEwayBillPDFProcessor;

    @Override
    public void configure() throws Exception {
        QueryConfiguration.Query query = queryConfiguration.findByName("listInvoices");
        rest()
                .get("/invoices")
                .produces(MediaType.TEXT_HTML)
                .route()
                .to("sql:" + query.getQuery() + "?dataSource=dataSource")
                .to("velocity:file://" + templateConfiguration.getPageTemplatePath() + "DisplayInvoices.vm");

        JacksonDataFormat format = new JacksonDataFormat();
        format.setUnmarshalType(Audit.class);
        format.enableFeature(DeserializationFeature.UNWRAP_ROOT_VALUE);
        rest()
                .get("/invoiceDetails/{tracking_id}")
                .produces(MediaType.TEXT_HTML)
                .route()
                .bean(auditService, "findDocumentsById")
                .to("velocity:file://" + templateConfiguration.getPageTemplatePath() + "DisplayInvoiceDetails.vm");

        rest()
                .get("/generateEwayBill/{trx_id}")
                .produces(MediaType.APPLICATION_OCTET_STREAM)
                .route()
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        String trxId = (String) exchange.getIn().getHeader("trx_id");
                        exchange.getIn().setHeader("trx_id", Integer.parseInt(trxId));
                    }
                })
                .to("sql:" + queryConfiguration.findByName("findInvoice").getQuery() + "?dataSource=dataSource")
                .process(generateEwayBillProcessor)
                .setBody(simple("${body.iterator().next()}"))
                .tracing().log("invoice found is: ${body}")
                //.bean(auditService, "findInvoice(${header.trx_id})")
                .to("velocity:file://" + templateConfiguration.getPageTemplatePath() + "EwayBill.vm")
                .setHeader("Content-disposition", constant("attachment; filename=EwayBill.pdf"))
                .process(generateEwayBillPDFProcessor);
    }

}
