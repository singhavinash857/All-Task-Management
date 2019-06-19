package com.transformedge.teewb.request;

import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.transformedge.teewb.processor.EwayBillProcessFailure;
import com.transformedge.teewb.processor.ProcessExceptionCodes;

@JsonRootName(value = "OutputResponse")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OutputResponse {

    private String status;
    private String data;
    private String ewayBillNo;
    private String ewayBillDate;
    private String validUpto;

    @JsonProperty("TotalRecordsCount")
    private String TotalRecordsCount;

    @JsonProperty("ProcessedRecordscount")
    private String ProcessedRecordscount;

    @JsonProperty("ErrorRecordsCount")
    private String ErrorRecordsCount;

    @JsonProperty("ErrorRecords")
    private List<Map<String, String>> ErrorRecords;

    private String errorCodes;

    private String error;
    
    @JsonProperty("errorDescription")
    private String errorDesciption;

    private ExceptionResponse exceptionResponse;

    public String getErrorCodes() {
        return errorCodes;
    }

    public void setErrorCodes(String errorCodes) {
        this.errorCodes = errorCodes;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getErrorDesciption() {
        return errorDesciption;
    }

    public void setErrorDesciption(String errorDesciption) {
        this.errorDesciption = errorDesciption;
    }

    public String getTotalRecordsCount() {
        return TotalRecordsCount;
    }

    public void setTotalRecordsCount(String totalRecordsCount) {
        TotalRecordsCount = totalRecordsCount;
    }

    public String getProcessedRecordscount() {
        return ProcessedRecordscount;
    }

    public void setProcessedRecordscount(String processedRecordscount) {
        ProcessedRecordscount = processedRecordscount;
    }

    public String getErrorRecordsCount() {
        return ErrorRecordsCount;
    }

    public void setErrorRecordsCount(String errorRecordsCount) {
        ErrorRecordsCount = errorRecordsCount;
    }

    public List<Map<String, String>> getErrorRecords() {
        return ErrorRecords;
    }

    public void setErrorRecords(List<Map<String, String>> errorRecords) {
        ErrorRecords = errorRecords;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getEwayBillNo() {
        return ewayBillNo;
    }

    public void setEwayBillNo(String ewayBillNo) {
        this.ewayBillNo = ewayBillNo;
    }

    public String getEwayBillDate() {
        return ewayBillDate;
    }

    public void setEwayBillDate(String ewayBillDate) {
        this.ewayBillDate = ewayBillDate;
    }

    public String getValidUpto() {
        return validUpto;
    }

    public void setValidUpto(String validUpto) {
        this.validUpto = validUpto;
    }

    public boolean isSuccess() {
        return ( (status != null && "1".equals(status)) ||
                 (ewayBillNo != null));
    }

    public ExceptionResponse getExceptionResponse() {
        return exceptionResponse;
    }

    public void setExceptionResponse(ExceptionResponse exceptionResponse) {
        this.exceptionResponse = exceptionResponse;
    }

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

    @Override
    public String toString() {
        return "OutputResponse{" +
                "status='" + status + '\'' +
                ", data='" + data + '\'' +
                ", ewayBillNo='" + ewayBillNo + '\'' +
                ", ewayBillDate='" + ewayBillDate + '\'' +
                ", validUpto='" + validUpto + '\'' +
                ", TotalRecordsCount='" + TotalRecordsCount + '\'' +
                ", ProcessedRecordscount='" + ProcessedRecordscount + '\'' +
                ", ErrorRecordsCount='" + ErrorRecordsCount + '\'' +
                ", ErrorRecords=" + ErrorRecords +
                ", errorCodes='" + errorCodes + '\'' +
                ", error='" + error + '\'' +
                ", errorDesciption='" + errorDesciption + '\'' +
                '}';
    }

    public static OutputResponse fromException(Exception ex) {
        StringWriter sw = new StringWriter();
        //ex.printStackTrace(new PrintWriter(sw));
        ExceptionResponse exceptionResponse = new ExceptionResponse(ex.getMessage(), sw.toString());
        OutputResponse outputResponse = new OutputResponse();
        outputResponse.setExceptionResponse(exceptionResponse);
        outputResponse.setError(ex.getMessage());
        outputResponse.setStatus("0");
        outputResponse.setErrorCodes(ProcessExceptionCodes.SYSTEM_ERROR.getErrorCode());
        outputResponse.setErrorDesciption(ex.getMessage());
        return outputResponse;
    }

    public static OutputResponse fromValidationException(EwayBillProcessFailure ex) {
        StringWriter sw = new StringWriter();
        //ex.printStackTrace(new PrintWriter(sw));
        ExceptionResponse exceptionResponse = new ExceptionResponse(ex.getMessage(), sw.toString());
        OutputResponse outputResponse = new OutputResponse();
        outputResponse.setExceptionResponse(exceptionResponse);
        outputResponse.setError(ex.getMessage());
        outputResponse.setStatus("0");
        outputResponse.setErrorCodes(ex.getErrorCode());
        outputResponse.setErrorDesciption(ex.getMessage());
        return outputResponse;
    }

    public static class ExceptionResponse {
        private String errorMessage;
        private String errorStackTrace;

        public ExceptionResponse(String errorMessage, String errorStackTrace) {
            this.errorMessage = errorMessage;
            this.errorStackTrace = errorStackTrace;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public String getErrorStackTrace() {
            return errorStackTrace;
        }

        public void setErrorStackTrace(String errorStackTrace) {
            this.errorStackTrace = errorStackTrace;
        }
    }
}
