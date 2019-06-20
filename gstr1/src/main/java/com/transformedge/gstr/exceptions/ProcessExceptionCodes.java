package com.transformedge.gstr.exceptions;

import lombok.Getter;
import lombok.ToString;

@ToString
public enum ProcessExceptionCodes {
    // system error...
    SYSTEM_ERROR("GSTS0001", "InternalServerError"),

    // validation error...
    VALIDATION_ERROR("GSTV0001", "InvalidInputData"),

    // invalid file content....
    INVALID_FILE_CONTENT("GSTV0002", "InvalidFileContent");

	@Getter
    private String errorCode;

    private ProcessExceptionCodes(String errorCode, String errorDescription) {
        this.errorCode = errorCode;
    }

}
