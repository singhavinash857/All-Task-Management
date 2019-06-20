package com.transformedge.gstr.processors.savegstr;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.transformedge.gstr.configuration.CsvConfiguration;
import com.transformedge.gstr.configuration.TableMetadataConfiguration;
import com.transformedge.gstr.request.CsvOutputData;

@Component
public class CsvDataProcessor implements Processor {

    @Autowired
    private CsvConfiguration csvConfiguration;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @SuppressWarnings("unchecked")
	@Override
    public void process(Exchange exchange) throws Exception {
		System.out.println("inside the CsvDataProcessor ::");

        CsvConfiguration.CsvConfig csvConfig = csvConfiguration.findByName("saveGstrForDocuments");
        TableMetadataConfiguration.Table table = csvConfig.getTableMetadataConfiguration().findByName("gstrTable");
        List<String> gstrTableColumns = table.getColumns();
        List<List<String>> csvData = (List<List<String>>) exchange.getIn().getBody();
        List<String> numericColumns = csvConfig.getValidation().getNumericColumns();
        List<Map<String, Object>> rowDataList = new ArrayList<Map<String, Object>>();
        logger.debug("CSV data read from file: " + exchange.getProperty("origFileName").toString() + " is: ");
        logger.debug(csvData.toString());
        for(List<String> values : csvData){
            Map<String, Object> row = new LinkedHashMap<>();
            for(int index = 0 ; index < gstrTableColumns.size() ; index++){
            	String columnName = gstrTableColumns.get(index);
            	if(numericColumns.contains(columnName)){
            		try{
                		row.put(columnName, NumberFormat.getInstance().parse(values.get(index)));
            		}catch(ParseException e){
                        logger.error("Invalid data format for field: " + columnName + ". It should be an numeric data");
            		}
            	}else{
                    row.put(columnName, values.get(index));
            	}
            }
            rowDataList.add(row);
        }
        CsvOutputData csvOutputData = new CsvOutputData(rowDataList);
        exchange.setProperty("outputCSVData", csvOutputData);
        exchange.getOut().setBody(rowDataList);
    }
}
