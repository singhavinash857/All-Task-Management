package com.transformedge.teewb.processor;

public class EwayBillProcessFailure extends RuntimeException {
    private ProcessExceptionCodes processExceptionCodes;

    public EwayBillProcessFailure(ProcessExceptionCodes processExceptionCodes, String message) {
        super(message);
        this.processExceptionCodes = processExceptionCodes;
    }

    public EwayBillProcessFailure(ProcessExceptionCodes processExceptionCodes, Throwable ex) {
        super(ex);
        this.processExceptionCodes = processExceptionCodes;
    }

    public String getErrorCode() {
        return processExceptionCodes.getErrorCode();
    }
}
