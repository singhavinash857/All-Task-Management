package com.transformedge.gstr.processors.savegstr;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

import com.transformedge.gstr.b2b.entities.Audit;

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
