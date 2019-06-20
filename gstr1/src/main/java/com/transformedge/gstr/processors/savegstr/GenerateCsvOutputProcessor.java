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
		
		if(exchange.getProperty("proccessCode") == "B2CS"){
			System.out.println("B2CS component in GenerateCsvOutputProcessor ::");
			com.transformedge.gstr.b2cs.entities.GstValue processedInvoice = exchange.getProperty("origInvoice", com.transformedge.gstr.b2cs.entities.GstValue.class);
		    gstinNumber = processedInvoice.getGstValue().getGstin();

		}else if(exchange.getProperty("proccessCode") == "B2B"){
			System.out.println("B2B component in GenerateCsvOutputProcessor::");
			GstValue processedInvoice = exchange.getProperty("origInvoice", GstValue.class);
			gstinNumber = processedInvoice.getGstValue().getGstin();
			
		}else if(exchange.getProperty("proccessCode") == "B2CL"){
			System.out.println("B2CL component in GenerateCsvOutputProcessor::");
			com.transformedge.gstr.b2cl.entities.GstValue processedInvoice = exchange.getProperty("origInvoice", com.transformedge.gstr.b2cl.entities.GstValue.class);
			gstinNumber = processedInvoice.getGstValue().getGstin();
			
		}else if(exchange.getProperty("proccessCode") == "AT"){
			System.out.println("AT component in GenerateCsvOutputProcessor::");
			com.transformedge.gstr.at.entities.GstValue processedInvoice = exchange.getProperty("origInvoice", com.transformedge.gstr.at.entities.GstValue.class);
			gstinNumber = processedInvoice.getGstValue().getGstin();
			
		}else if(exchange.getProperty("proccessCode") == "TXPD"){
			System.out.println("TXPD component in GenerateCsvOutputProcessor::");
			com.transformedge.gstr.txpd.entities.GstValue processedInvoice = exchange.getProperty("origInvoice", com.transformedge.gstr.txpd.entities.GstValue.class);
			gstinNumber = processedInvoice.getGstValue().getGstin();
			
		}else if(exchange.getProperty("proccessCode") == "EXP"){
			System.out.println("EXP component in GenerateCsvOutputProcessor::");
			com.transformedge.gstr.exp.entities.GstValue processedInvoice = exchange.getProperty("origInvoice", com.transformedge.gstr.exp.entities.GstValue.class);
			gstinNumber = processedInvoice.getGstValue().getGstin();
			
		}else if(exchange.getProperty("proccessCode") == "HSN"){
			System.out.println("HSN component in GenerateCsvOutputProcessor::");
			com.transformedge.gstr.hsn.entities.GstValue processedInvoice = exchange.getProperty("origInvoice", com.transformedge.gstr.hsn.entities.GstValue.class);
			gstinNumber = processedInvoice.getGstValue().getGstin();
			
		}else if(exchange.getProperty("proccessCode") == "CDNUR"){
			System.out.println("CDNUR component in GenerateCsvOutputProcessor::");
			com.transformedge.gstr.cdnur.entities.GstValue processedInvoice = exchange.getProperty("origInvoice", com.transformedge.gstr.cdnur.entities.GstValue.class);
			gstinNumber = processedInvoice.getGstValue().getGstin();
			
		}else if(exchange.getProperty("proccessCode") == "CDNR"){
			System.out.println("CDNR component in GenerateCsvOutputProcessor::");
			com.transformedge.gstr.cdnr.entities.GstValue processedInvoice = exchange.getProperty("origInvoice", com.transformedge.gstr.cdnr.entities.GstValue.class);
			gstinNumber = processedInvoice.getGstValue().getGstin();
			
		}else if(exchange.getProperty("proccessCode") == "NIL"){
			System.out.println("NIL component in GenerateCsvOutputProcessor::");
			com.transformedge.gstr.nil.entities.GstValue processedInvoice = exchange.getProperty("origInvoice", com.transformedge.gstr.nil.entities.GstValue.class);
			gstinNumber = processedInvoice.getGstValue().getGstin();
			
		}else if(exchange.getProperty("proccessCode") == "DOC_ISSUE"){
			System.out.println("DOC_ISSUE component in GenerateCsvOutputProcessor::");
			com.transformedge.gstr.docissue.entities.GstValue processedInvoice = exchange.getProperty("origInvoice", com.transformedge.gstr.docissue.entities.GstValue.class);
			gstinNumber = processedInvoice.getGstValue().getGstin();
			
		}else{
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
