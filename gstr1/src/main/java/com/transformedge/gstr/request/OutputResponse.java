package com.transformedge.gstr.request;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.transformedge.gstr.exceptions.EwayBillProcessFailure;

import lombok.Data;

@Data
@JsonRootName(value = "OutputResponse")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OutputResponse {

	@JsonProperty("Actions")
	private List<Actions> actions;
	
    private ExceptionResponse exceptionResponse;

	public String toJSON() {
		ObjectMapper mapper  = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
		try {
			return mapper.writeValueAsString(this);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static OutputResponse fromValidationException(EwayBillProcessFailure ex) {
		StringWriter sw = new StringWriter();
		ExceptionResponse exceptionResponse = new ExceptionResponse(ex.getMessage(), sw.toString());
		OutputResponse outputResponse = new OutputResponse();
		//outputResponse.setExceptionResponse(exceptionResponse);
		
		Actions actions  = new Actions();
		
		Details details = new  Details();
		Map<String,String> errorRecordMap = new HashMap<>();
		
		details.setErrorRecordsCount(1);
		errorRecordMap.put("errormessage", ex.getMessage());
		errorRecordMap.put("errorcode", ex.getErrorCode());
		
		List<Map<String,String>> errorRecordList = new ArrayList<>();
		errorRecordList.add(errorRecordMap);
		
		details.setErrorRecords(errorRecordList);
		
		List<Actions> actionList = new ArrayList<>();
		actionList.add(actions);
		
		List<Details> detailsList = new ArrayList<>();
		detailsList.add(details);
		
		actions.setDetails(detailsList);
		
		outputResponse.setActions(actionList);
		/*
		 * "OutputResponse": {
        "Actions": [
            {
                "ActionType": "B2B",
                "Details": [
                    {
                        "TotalRecordsCount": 5,
                        "ProcessedRecordscount": 4,
                        "ErrorRecordsCount": 1,
                        "ErrorRecords": [{
                         "errormessage": "Invalid Invoice Date",
                                "errorcode": "-7"
                        }]
                     }
                           ]
            }
                    ]
                        
                                }
		 */
			
		System.out.println("outputResponse.toJSON() ::::::"+outputResponse.toJSON());
		return outputResponse;
	}

//	public static OutputResponse fromException(Exception ex) {
//		StringWriter sw = new StringWriter();
//		//ex.printStackTrace(new PrintWriter(sw));
//		ExceptionResponse exceptionResponse = new ExceptionResponse(ex.getMessage(), sw.toString());
//		OutputResponse outputResponse = new OutputResponse();
//		outputResponse.setExceptionResponse(exceptionResponse);
//		outputResponse.setError(ex.getMessage());
//		outputResponse.setStatus("0");
//		outputResponse.setErrorCodes(ProcessExceptionCodes.SYSTEM_ERROR.getErrorCode());
//		outputResponse.setErrorDesciption(ex.getMessage());
//		return outputResponse;
//	}

	@Data
	public static class ExceptionResponse {
		private String errorMessage;
		private String errorStackTrace;

		public ExceptionResponse(String errorMessage, String errorStackTrace) {
			this.errorMessage = errorMessage;
			this.errorStackTrace = errorStackTrace;
		}

	}

}
