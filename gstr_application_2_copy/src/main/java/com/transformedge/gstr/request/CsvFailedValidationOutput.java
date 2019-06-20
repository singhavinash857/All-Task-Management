package com.transformedge.gstr.request;

import com.transformedge.gstr.exceptions.GstrFailedInvoice;
import com.transformedge.gstr.exceptions.ProcessExceptionCodes;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;

@Data
@ToString
public class CsvFailedValidationOutput {

	@Getter
    private GstrFailedInvoice failedInvoice;
    private String errorMessage;
    @Getter
    private ProcessExceptionCodes processExceptionCodes;

    public CsvFailedValidationOutput(GstrFailedInvoice failedInvoices, String errorMessage, ProcessExceptionCodes processExceptionCodes) {
        this.failedInvoice = failedInvoices;
        this.errorMessage = errorMessage;
        this.processExceptionCodes = processExceptionCodes;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

	public boolean alreadyExist(String string, String identifierColumn) {
		// TODO Auto-generated method stub
		return false;
	}
   
}
