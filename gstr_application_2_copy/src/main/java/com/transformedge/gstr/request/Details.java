package com.transformedge.gstr.request;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class Details {
	
    @JsonProperty("TotalRecordsCount")
	private int totalRecordsCount;
    
    @JsonProperty("ErrorRecordsCount")
	private int errorRecordsCount;
	
    @JsonProperty("ErrorRecords")
	private List<Map<String, String>> errorRecords;
    
    @JsonProperty("ProcessedRecordscount")
	private int processedRecordscount;
	
	
}
