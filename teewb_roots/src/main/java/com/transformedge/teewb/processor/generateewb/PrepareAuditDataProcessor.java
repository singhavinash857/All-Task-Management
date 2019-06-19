package com.transformedge.teewb.processor.generateewb;

import com.transformedge.teewb.entity.Audit;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class PrepareAuditDataProcessor implements Processor {

    @Override
    public void process(Exchange exchange) throws Exception {
        Audit audit = new Audit();
        audit.setRequest((String) exchange.getProperty("requestJSON"));
        audit.setResponse((String) exchange.getProperty("responseJSON"));
        audit.setInvoiceId((String) exchange.getProperty("trackingId"));
        audit.setType("audit");
        exchange.getOut().setBody(audit.toJSON());
    }
}
