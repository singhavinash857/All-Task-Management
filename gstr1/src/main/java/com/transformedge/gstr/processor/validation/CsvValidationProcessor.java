package com.transformedge.gstr.processor.validation;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.transformedge.gstr.configuration.CsvConfiguration;
import com.transformedge.gstr.configuration.TableMetadataConfiguration;
import com.transformedge.gstr.exceptions.GstrFailedInvoice;
import com.transformedge.gstr.exceptions.ProcessExceptionCodes;
import com.transformedge.gstr.request.CsvFailedValidationOutput;

@Component
public class CsvValidationProcessor implements Processor {

	@Autowired
	private CsvConfiguration csvConfiguration;
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private static final String regexPatternNumeric = "^([0-9]+\\.?[0-9]*|[0-9]*\\.[0-9]+)$";

	@Override
	public void process(Exchange exchange) throws Exception {
		System.out.println("inside the CsvValidationProcessor ::");

		CsvConfiguration.CsvConfig csvConfig = csvConfiguration.findByName("saveGstrForDocuments");
		TableMetadataConfiguration.Table table = csvConfig.getTableMetadataConfiguration().findByName("gstrTable");
		List<String> numericColumns = csvConfig.getValidation().getNumericColumns();
		List<String> gstrTableColumns = table.getColumns();
		List<CsvFailedValidationOutput> failedValidationOutputs = new ArrayList<>();
		List<List<String>> filteredCsvData = new ArrayList<>();

		@SuppressWarnings("unchecked")
		List<List<String>> csvData = (List<List<String>>) exchange.getIn().getBody();
		outer : for(List<String> csvValues : csvData){

			boolean isValidCsvData = true;
			for (CsvFailedValidationOutput csvFailedValidationOutput : failedValidationOutputs) {
				if(csvFailedValidationOutput.getFailedInvoice().getGstin().equalsIgnoreCase(csvValues.get(1))){
					continue outer;
				}
			}

			for(int index = 0 ; index <  gstrTableColumns.size() ; index++){
				String columnName = gstrTableColumns.get(index);
				if(numericColumns.contains(columnName)){
					if(!(csvValues.get(index).matches(regexPatternNumeric))){
						logger.info("gstin : " + csvValues.get(1) + " has invalid data for field: " + columnName);
						GstrFailedInvoice gstrFailedInvoice = new GstrFailedInvoice();
						gstrFailedInvoice.setGstin(csvValues.get(1));
					//	gstrFailedInvoice.setActionType(actionType);
						
						CsvFailedValidationOutput csvFailedValidationOutput = new CsvFailedValidationOutput(gstrFailedInvoice,
								"Field: " + columnName + " should be a number but has invalid value: " + csvValues.get(index),
								ProcessExceptionCodes.VALIDATION_ERROR);

						failedValidationOutputs.add(csvFailedValidationOutput);
						isValidCsvData = false;
					}
				}
			}
			if(isValidCsvData)
				filteredCsvData.add(csvValues);
		}
		exchange.setProperty("validationFailedInvoices", failedValidationOutputs);
		exchange.getOut().setBody(filteredCsvData);
	}

}
