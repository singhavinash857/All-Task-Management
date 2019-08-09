package com.transformedge.gstr.routes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.transformedge.gstr.b2b.entities.GstValue;
import com.transformedge.gstr.b2b.entities.TransactionAudit;
import com.transformedge.gstr.configuration.ApiConfiguration;
import com.transformedge.gstr.configuration.AuditConfiguration;
import com.transformedge.gstr.configuration.CsvConfiguration;
import com.transformedge.gstr.configuration.TableMetadataConfiguration;
import com.transformedge.gstr.exceptions.EwayBillProcessFailure;
import com.transformedge.gstr.exceptions.GstrFailedInvoice;
import com.transformedge.gstr.exceptions.ProcessExceptionCodes;
import com.transformedge.gstr.processor.validation.CsvValidationProcessor;
import com.transformedge.gstr.processors.savegstr.CsvDataProcessor;
import com.transformedge.gstr.processors.savegstr.GenerateCsvOutputProcessor;
import com.transformedge.gstr.processors.savegstr.PrepareAuditDataProcessor;
import com.transformedge.gstr.processors.savegstr.SaveGstrProcessor;
import com.transformedge.gstr.request.CsvFailedValidationOutput;
import com.transformedge.gstr.request.CsvOutputData;
import com.transformedge.gstr.request.OutputResponse;
import com.transformedge.gstr.services.AuditService;

@Component
public class SaveGstr1CsvRoutes extends RouteBuilder{							

	@Autowired
	private CsvConfiguration csvConfiguration;

	@Autowired
	private SaveGstrProcessor saveGstrProcessor;

	@Autowired
	private CsvDataProcessor csvDataProcessor;

	@Autowired
	private CsvValidationProcessor csvValidationProcessor;

	@Autowired
	private AuditService auditService;

	@Autowired
	AuditConfiguration auditConfiguration;

	@Autowired
	ApiConfiguration  apiConfiguration;

	@Autowired
	private PrepareAuditDataProcessor prepareAuditDataProcessor;

	@Autowired
	private GenerateCsvOutputProcessor generateCsvOutputProcessor;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@SuppressWarnings("unchecked")
	@Override
	public void configure() throws Exception {
		logger.info("started here !!!");

		CsvConfiguration.CsvConfig csvConfig = csvConfiguration.findByName("saveGstrForDocuments");
		TableMetadataConfiguration.Table table = csvConfig.getTableMetadataConfiguration().findByName("gstrTable");

		onException(IllegalStateException.class, IOException.class)
		// Calling Endpoint that will process the invalid data in csv..
		.to("direct:processInvalidFile");

		// Reading the file from input folder..
		from("file://" + csvConfig.getInputPath() + "?initialDelay=" + csvConfig.getInitialDelay()+"&delay="+csvConfig.getConsumeDelay()+"&move=.done")
		.routeId("CSVPoller")
		.autoStartup(false)
		.log(LoggingLevel.INFO, "Processing CSV file ${header.CamelFileNameOnly}")
		.setProperty("origFileName", simple("${header.CamelFileNameOnly}"))
		.unmarshal(new CsvDataFormat("|"))
		// Checking the validations of csv data..(numeric data,date type etc..)
		.process(csvValidationProcessor)
		.log("CSV data after validation: ${body}")
		.log("validation failed invoices: ${exchangeProperty.validationFailedInvoices}")
		// Keeping valid and invalid data in..
		.process(csvDataProcessor)
		.log("CSV data processing: ${body}")
		.log("outputCSVData : ${exchangeProperty.outputCSVData}")
		// Creating the json from the csv data..
		.process(saveGstrProcessor)
		.split(body()).streaming()
		   .log("original gstrInvoice : ${body}")
		   .setProperty("origInvoice", simple("${body}"))
		   // Calling Endpoint to store the invoice details in database..
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
                logger.debug("processor:: " + exchange.getIn().getBody().getClass().getName());
               
                CsvFailedValidationOutput validationOutput = exchange.getIn().getBody(CsvFailedValidationOutput.class);
                GstrFailedInvoice  gstrFailedInvoice = validationOutput.getFailedInvoice();
                String gstinNumber = gstrFailedInvoice.getGstin();
                Map<String, Object> map = new HashMap<>();
                map.put(table.getIdentifierColumn() , gstinNumber);
                
                csvOutputData.addData(map);
                
                csvOutputData.addDataToRows(table.getIdentifierColumn(), gstinNumber,
    					"gstin_number", gstinNumber);
                
                csvOutputData.addDataToRows(table.getIdentifierColumn(), gstinNumber,
    					"gstin_Process_status", "E");
                
    			csvOutputData.addDataToRows(table.getIdentifierColumn(), gstinNumber,
                        "gstin_action_type", "");
               
                csvOutputData.addDataToRows(table.getIdentifierColumn(), gstinNumber,
                        "gstin_error_code", validationOutput.getProcessExceptionCodes().getErrorCode());
               
                csvOutputData.addDataToRows(table.getIdentifierColumn(), gstinNumber,
                        "gstin_error_message", validationOutput.getErrorMessage());
                
                csvOutputData.addDataToRows(table.getIdentifierColumn(), gstinNumber,
                		"e_process_status", "E");
                
                 EwayBillProcessFailure exception = new EwayBillProcessFailure(validationOutput.getProcessExceptionCodes(),
                                                            validationOutput.getErrorMessage());
               
                exchange.setProperty("EwayBillException", exception);

            })
            .to("direct:processExceptionResponseForCsv")
        .end()
		.end()
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

		from("direct:createCsvOutput")
		.marshal(csvDataFormat)
		.to("file://" + csvConfig.getOutputPath() + "?fileName=${exchangeProperty.origFileName}")
		.end();

		// Storing the invoice into the database, getting single invoice at a time....
		from("direct:persistRequestInvoiceForCsv")
		.routeId("PersistRequestInvoiceForCsv")	
		.process(exchange -> {
			String gstinNumber = null;
			
         //   GstrFailedInvoice origInvoice = exchange.getIn().getBody(GstrFailedInvoice.class);
			if(exchange.getIn().getBody(GstrFailedInvoice.class) != null){
				gstinNumber = exchange.getIn().getBody(GstrFailedInvoice.class).getGstin();
			
			}else if(exchange.getProperty("proccessCode") == "B2CS"){
				com.transformedge.gstr.b2cs.entities.GstValue origGstrInvoice = exchange.getIn().getBody(com.transformedge.gstr.b2cs.entities.GstValue.class);
				gstinNumber = origGstrInvoice.getGstValue().getGstin();
			
			}else if(exchange.getProperty("proccessCode") == "B2B"){
				GstValue origGstrInvoice = exchange.getIn().getBody(GstValue.class);
				gstinNumber = origGstrInvoice.getGstValue().getGstin();
			
			}else if(exchange.getProperty("proccessCode") == "B2CL"){
				com.transformedge.gstr.b2cl.entities.GstValue origGstrInvoice = exchange.getIn().getBody(com.transformedge.gstr.b2cl.entities.GstValue.class);
				gstinNumber = origGstrInvoice.getGstValue().getGstin();
			
			}else if(exchange.getProperty("proccessCode") == "AT"){
				com.transformedge.gstr.at.entities.GstValue origGstrInvoice = exchange.getIn().getBody(com.transformedge.gstr.at.entities.GstValue.class);
				gstinNumber = origGstrInvoice.getGstValue().getGstin();
			
			}else if(exchange.getProperty("proccessCode") == "TXPD"){
				com.transformedge.gstr.txpd.entities.GstValue origGstrInvoice = exchange.getIn().getBody(com.transformedge.gstr.txpd.entities.GstValue.class);
				gstinNumber = origGstrInvoice.getGstValue().getGstin();
			
			}else if(exchange.getProperty("proccessCode") == "EXP"){
				System.out.println("EXP component ::");
				com.transformedge.gstr.exp.entities.GstValue origGstrInvoice = exchange.getIn().getBody(com.transformedge.gstr.exp.entities.GstValue.class);
				gstinNumber = origGstrInvoice.getGstValue().getGstin();
			
			}else if(exchange.getProperty("proccessCode") == "HSN"){
				System.out.println("HSN component ::");
				com.transformedge.gstr.hsn.entities.GstValue origGstrInvoice = exchange.getIn().getBody(com.transformedge.gstr.hsn.entities.GstValue.class);
				gstinNumber = origGstrInvoice.getGstValue().getGstin();
			
			}else if(exchange.getProperty("proccessCode") == "CDNUR"){
				com.transformedge.gstr.cdnur.entities.GstValue origGstrInvoice = exchange.getIn().getBody(com.transformedge.gstr.cdnur.entities.GstValue.class);
				gstinNumber = origGstrInvoice.getGstValue().getGstin();
			
			}else if(exchange.getProperty("proccessCode") == "CDNR"){
				com.transformedge.gstr.cdnr.entities.GstValue origGstrInvoice = exchange.getIn().getBody(com.transformedge.gstr.cdnr.entities.GstValue.class);
				gstinNumber = origGstrInvoice.getGstValue().getGstin();
			
			}else if(exchange.getProperty("proccessCode") == "NIL"){
				com.transformedge.gstr.nil.entities.GstValue origGstrInvoice = exchange.getIn().getBody(com.transformedge.gstr.nil.entities.GstValue.class);
				gstinNumber = origGstrInvoice.getGstValue().getGstin();
			
			}else if(exchange.getProperty("proccessCode") == "DOC_ISSUE"){
				com.transformedge.gstr.docissue.entities.GstValue origGstrInvoice = exchange.getIn().getBody(com.transformedge.gstr.docissue.entities.GstValue.class);
				gstinNumber = origGstrInvoice.getGstValue().getGstin();
			
			}else{
				logger.error("NULL component in  TransactionAudit in SaveGstr1CsvRoutes");
			}
			TransactionAudit existingTx = auditService.findTransactionAudit(gstinNumber);
			if (existingTx == null) {
				logger.info("Invoice with number: " + gstinNumber + " doesn't exist in database. Creating new Invoice record");
				TransactionAudit invoiceAudit = new TransactionAudit();
				invoiceAudit.setType("transaction");
				invoiceAudit.setGstin(gstinNumber);
				exchange.setProperty("isNew", true);
				exchange.getOut().setBody(invoiceAudit.toJSON());
			} else {
				logger.info("Invoice with number: " + gstinNumber + " already exists in database");
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

		// Endpoint to processing the invalid data in the csv file..
		from("direct:processInvalidFile")
		.log("The CSV file is invalid. Please check the data")
		.process(exchange -> {
			Throwable throwable = exchange.getProperty("CamelExceptionCaught", Throwable.class);
			List<String> outputColuimns = csvConfig.getOutputColumns();
			List<Map<String,String>> data = new ArrayList<Map<String, String>>(1);
			Map<String, String> row = new LinkedHashMap<String, String>();
			for (String columnName: outputColuimns) {
				switch (columnName)  {
				case "gstin_Process_status" :
					row.put(columnName, "E");
					break;
				case "gstin_error_code" :
					row.put(columnName, ProcessExceptionCodes.INVALID_FILE_CONTENT.getErrorCode());
					break;
				case "gstin_error_message" :
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


		from("direct:prepareRequestJSONForCsv")
		.routeId("PrepareRequestJSONRouteForCsv")
		.tracing().log("Processing invoices : ${body}")
		.marshal()
		.json(JsonLibrary.Jackson)
		.convertBodyTo(String.class)
		.setProperty("requestJSON", simple("${body}"))
		.log(LoggingLevel.INFO, "Save GSTR request JSON is: ${body}")
		// Calling Endpoint..
		.to("direct:callSaveGstr1APIForCsv")
		.end();

		ApiConfiguration.Api api = apiConfiguration.findByName("saveGSTR");
		JacksonDataFormat format = new JacksonDataFormat();
		format.setUnmarshalType(OutputResponse.class);
		format.enableFeature(DeserializationFeature.UNWRAP_ROOT_VALUE);

		// Endpoint to call the real api and sending json with headers as request..
		from("direct:callSaveGstr1APIForCsv")
		.routeId("InvokeEwayBillApiRouteForCsv")
		.process(exchange -> {
			Message in = exchange.getIn();
			Map<String, Object> newMap = api.getHeaders().entrySet().stream()
					.collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue()));
			in.setHeaders(newMap);
		})
		
		.doTry()
		.to(api.getUrl())
		.unmarshal(format)
		.log(LoggingLevel.INFO, "Response from saveGstr API is: ${body.toJSON()}")
		// Calling the endpoint to storing the response into the csv file..
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
		.endDoTry(); // end of api calling endpoint

		// Endpoit to storing the api response into the output csv file..
		from("direct:processResponseForCsv")
		.routeId("PrepareAuditDataRouteForCsv")
		.setProperty("responseJSON", simple("${body.toJSON()}"))
		// Creating the output data format in csv..
		.process(generateCsvOutputProcessor)
		.log(LoggingLevel.INFO, "Preparing CSV response for file ${exchangeProperty.origFileName}")
		.to("direct:persistAuditForCsv");

		 from("direct:processExceptionResponseForCsv")
         .routeId("ProcessErrorResponseForCsv")
         .bean(OutputResponse.class, "fromValidationException(${exchangeProperty.EwayBillException})")
         .process(generateCsvOutputProcessor)
         .setProperty("responseJSON", simple("${body.toJSON()}"))
         // Calling endpoint for storing the response into the database..
         .to("direct:persistAuditForCsv");
         
		// Endpoint to storing the Api response also into the database..
		from("direct:persistAuditForCsv")
		.routeId("PersistAuditDataRouteForCsv")
		.marshal().json(JsonLibrary.Jackson)
		.process(prepareAuditDataProcessor)
		.to("couchdb:" + auditConfiguration.getUrl())
		.process(new Processor() {
			@Override
			public void process(Exchange exchange) throws Exception {
				String gstinNum = null;
				
				if(exchange.getProperty("proccessCode") == "B2CS"){
					com.transformedge.gstr.b2cs.entities.GstValue processedInvoice = exchange.getProperty("origInvoice", com.transformedge.gstr.b2cs.entities.GstValue.class);
					gstinNum = (String) processedInvoice.getGstValue().getGstin();
				
				}else if(exchange.getProperty("proccessCode") == "B2B"){
					GstValue processedInvoice = exchange.getProperty("origInvoice", GstValue.class);
					gstinNum = (String) processedInvoice.getGstValue().getGstin();
				
				}else if(exchange.getProperty("proccessCode") == "B2CL"){
					com.transformedge.gstr.b2cl.entities.GstValue processedInvoice = exchange.getProperty("origInvoice", com.transformedge.gstr.b2cl.entities.GstValue.class);
					gstinNum = (String) processedInvoice.getGstValue().getGstin();
				
				}else if(exchange.getProperty("proccessCode") == "AT"){
					com.transformedge.gstr.at.entities.GstValue processedInvoice = exchange.getProperty("origInvoice", com.transformedge.gstr.at.entities.GstValue.class);
					gstinNum = (String) processedInvoice.getGstValue().getGstin();
				
				}else if(exchange.getProperty("proccessCode") == "TXPD"){
					com.transformedge.gstr.txpd.entities.GstValue processedInvoice = exchange.getProperty("origInvoice", com.transformedge.gstr.txpd.entities.GstValue.class);
					gstinNum = (String) processedInvoice.getGstValue().getGstin();
				
				}else if(exchange.getProperty("proccessCode") == "TXPD"){
					com.transformedge.gstr.txpd.entities.GstValue processedInvoice = exchange.getProperty("origInvoice", com.transformedge.gstr.txpd.entities.GstValue.class);
					gstinNum = (String) processedInvoice.getGstValue().getGstin();

				}else if(exchange.getProperty("proccessCode") == "HSN"){
					com.transformedge.gstr.hsn.entities.GstValue processedInvoice = exchange.getProperty("origInvoice", com.transformedge.gstr.hsn.entities.GstValue.class);
					gstinNum = (String) processedInvoice.getGstValue().getGstin();

				}else if(exchange.getProperty("proccessCode") == "HSN"){
					com.transformedge.gstr.hsn.entities.GstValue processedInvoice = exchange.getProperty("origInvoice", com.transformedge.gstr.hsn.entities.GstValue.class);
					gstinNum = (String) processedInvoice.getGstValue().getGstin();

				}else if(exchange.getProperty("proccessCode") == "CDNUR"){
					com.transformedge.gstr.cdnur.entities.GstValue processedInvoice = exchange.getProperty("origInvoice", com.transformedge.gstr.cdnur.entities.GstValue.class);
					gstinNum = (String) processedInvoice.getGstValue().getGstin();

				}else if(exchange.getProperty("proccessCode") == "CDNR"){
					com.transformedge.gstr.cdnr.entities.GstValue processedInvoice = exchange.getProperty("origInvoice", com.transformedge.gstr.cdnr.entities.GstValue.class);
					gstinNum = (String) processedInvoice.getGstValue().getGstin();

				}else if(exchange.getProperty("proccessCode") == "NIL"){
					com.transformedge.gstr.nil.entities.GstValue processedInvoice = exchange.getProperty("origInvoice", com.transformedge.gstr.nil.entities.GstValue.class);
					gstinNum = (String) processedInvoice.getGstValue().getGstin();

				}else if(exchange.getProperty("proccessCode") == "DOC_ISSUE"){
					com.transformedge.gstr.docissue.entities.GstValue processedInvoice = exchange.getProperty("origInvoice", com.transformedge.gstr.docissue.entities.GstValue.class);
					gstinNum = (String) processedInvoice.getGstValue().getGstin();

				}else{
					GstrFailedInvoice processedInvoice = exchange.getProperty("origInvoice", GstrFailedInvoice.class);
					gstinNum = processedInvoice.getGstin();
					logger.error("Please check you SaveGstr1CsvRoutes exchange "+processedInvoice+"and gstin number :"+gstinNum);
				}
				
				CsvOutputData csvOutputData = (CsvOutputData) exchange.getProperty("outputCSVData");
				csvOutputData.addDataToRows(table.getIdentifierColumn(), gstinNum, "intg_tracking_id", exchange.getIn().getHeader("CouchDbId"));
			}
		})
		.end()
		.log(LoggingLevel.INFO, "Generate Eway Bill process completed !!!");

	}
}
