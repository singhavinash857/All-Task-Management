package com.transformedge.teewb.processor.reports;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;

@Component
public class GenerateEwayBillPDFProcessor implements Processor {

    @Override
    public void process(Exchange exchange) throws Exception {
        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(exchange.getIn().getBody().toString());
        renderer.layout();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        renderer.createPDF(outputStream);
        exchange.getIn().setBody(outputStream);
    }
}
