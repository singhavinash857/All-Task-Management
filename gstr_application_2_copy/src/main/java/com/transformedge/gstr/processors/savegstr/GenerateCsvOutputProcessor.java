package com.transformedge.gstr.processors.savegstr;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.transformedge.gstr.b2b.entities.GstValue;
import com.transformedge.gstr.configuration.CsvConfiguration;
import com.transformedge.gstr.configuration.TableMetadataConfiguration;
import com.transformedge.gstr.request.CsvOutputData;
import com.transformedge.gstr.request.OutputResponse;

@Component
public class GenerateCsvOutputProcessor implements Processor {

	@Autowired
	private CsvConfiguration csvConfiguration;
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());


	@Override
	public void process(Exchange exchange) throws Exception {

		TableMetadataConfiguration.Table invoiceTable = csvConfiguration.findByName("saveGstrForDocuments")
				.getTableMetadataConfiguration().findByName("gstrTable");
		
		String gstinNumber = null;
		
		CsvOutputData csvOutputData = (CsvOutputData) exchange.getProperty("outputCSVData");
		
		System.out.println("csvOutputData testing ::::"+csvOutputData.toString());
		
	    if(exchange.getProperty("proccessCode") == "B2B"){
			System.out.println("B2B component in GenerateCsvOutputProcessor::");
			GstValue processedInvoice = exchange.getProperty("origInvoice", GstValue.class);
			gstinNumber = processedInvoice.getGstValue().getGstin();
			
		}else if(exchange.getProperty("proccessCode") == "B2BUR"){
			System.out.println("B2BUR component in GenerateCsvOutputProcessor::");
			com.transformedge.gstr.b2bur.entities.GstValue processedInvoice = exchange.getProperty("origInvoice", com.transformedge.gstr.b2bur.entities.GstValue.class);
			gstinNumber = processedInvoice.getGstValue().getGstin();

		}
	    else if(exchange.getProperty("proccessCode") == "HSNSUM"){
			System.out.println("HSNSUM component in GenerateCsvOutputProcessor::");
			com.transformedge.gstr.hsnsum.entities.GstValue processedInvoice = exchange.getProperty("origInvoice", com.transformedge.gstr.hsnsum.entities.GstValue.class);
			gstinNumber = processedInvoice.getGstValue().getGstin();

		}else if(exchange.getProperty("proccessCode") == "CDN"){
			System.out.println("CDN component in GenerateCsvOutputProcessor::");
			com.transformedge.gstr.cdn.entities.GstValue processedInvoice = exchange.getProperty("origInvoice", com.transformedge.gstr.cdn.entities.GstValue.class);
			gstinNumber = processedInvoice.getGstValue().getGstin();

		}else if(exchange.getProperty("proccessCode") == "CDNUR"){
			System.out.println("CDNUR component in GenerateCsvOutputProcessor::");
			com.transformedge.gstr.cdnur.entities.GstValue processedInvoice = exchange.getProperty("origInvoice", com.transformedge.gstr.cdnur.entities.GstValue.class);
			gstinNumber = processedInvoice.getGstValue().getGstin();

		}else if(exchange.getProperty("proccessCode") == "IMPG"){
			System.out.println("IMPG component in GenerateCsvOutputProcessor::");
			com.transformedge.gstr.impg.entities.GstValue processedInvoice = exchange.getProperty("origInvoice", com.transformedge.gstr.impg.entities.GstValue.class);
			gstinNumber = processedInvoice.getGstValue().getGstin();

		}
		else{
			logger.error("output data cant create, please check you component in GenerateCsvOutputProcessor !!");
		}
		OutputResponse outputResponse = exchange.getIn().getBody(OutputResponse.class);
		System.out.println("outputResponse testing ::::::"+outputResponse);
		int errorCount = outputResponse.getActions().get(0).getDetails().get(0).getErrorRecordsCount();
		if(errorCount == 0){
			csvOutputData.addDataToRows(invoiceTable.getIdentifierColumn(), gstinNumber,
					"gstin_number", gstinNumber);
			csvOutputData.addDataToRows(invoiceTable.getIdentifierColumn(), gstinNumber, "gstin_action_type", outputResponse.getActions().get(0).getActionType());
			csvOutputData.addDataToRows(invoiceTable.getIdentifierColumn(), gstinNumber, "gstin_error_message","");
			csvOutputData.addDataToRows(invoiceTable.getIdentifierColumn(), gstinNumber, "gstin_error_code", "");
			csvOutputData.addDataToRows(invoiceTable.getIdentifierColumn(), gstinNumber, "gstin_Process_status", "P");
		}else if(errorCount > 0){
			csvOutputData.addDataToRows(invoiceTable.getIdentifierColumn(), gstinNumber,
					"gstin_number", gstinNumber);
			csvOutputData.addDataToRows(invoiceTable.getIdentifierColumn(), gstinNumber, "gstin_action_type", outputResponse.getActions().get(0).getActionType());
			csvOutputData.addDataToRows(invoiceTable.getIdentifierColumn(), gstinNumber, "gstin_error_message", outputResponse.getActions().get(0).getDetails().get(0).getErrorRecords().get(0).get("errormessage"));
			csvOutputData.addDataToRows(invoiceTable.getIdentifierColumn(), gstinNumber, "gstin_error_code", outputResponse.getActions().get(0).getDetails().get(0).getErrorRecords().get(0).get("errorcode"));
			csvOutputData.addDataToRows(invoiceTable.getIdentifierColumn(), gstinNumber, "gstin_Process_status", "E");
		}
	}
}
