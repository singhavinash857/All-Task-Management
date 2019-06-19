package com.transformedge.teewb.request;

import com.transformedge.teewb.processor.ProcessExceptionCodes;

/**
 * Created by chandrasekarramaswamy on 26/06/18.
 */
public class CsvFailedValidationOutput {

    private Invoice failedInvoice;
    private String errorMessage;
    private ProcessExceptionCodes processExceptionCodes;

    public CsvFailedValidationOutput(Invoice failedInvoices, String errorMessage, ProcessExceptionCodes processExceptionCodes) {
        this.failedInvoice = failedInvoices;
        this.errorMessage = errorMessage;
        this.processExceptionCodes = processExceptionCodes;
    }

    public Invoice getFailedInvoice() {
        return failedInvoice;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public ProcessExceptionCodes getProcessExceptionCodes() {
        return processExceptionCodes;
    }

    public boolean hasInvoice(String invoiceId, String idColumnName) {
        // getting value by the key from the map using "failedInvoice.getFieldValue(idColumnName)" and checking if it is
       // equal to invoiceid
        return invoiceId.equalsIgnoreCase(String.valueOf(failedInvoice.getFieldValue(idColumnName)));
    }

    @Override
    public String toString() {
        return "CsvFailedValidationOutput{" +
                "failedInvoice=" + failedInvoice +
                ", errorMessage='" + errorMessage + '\'' +
                ", processExceptionCodes=" + processExceptionCodes +
                '}';
    }
}
