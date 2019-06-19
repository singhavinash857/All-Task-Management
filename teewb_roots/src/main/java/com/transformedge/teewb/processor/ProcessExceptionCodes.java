package com.transformedge.teewb.processor;

public enum ProcessExceptionCodes {
	SYSTEM_ERROR("EWBS0001", "InternalServerError"),
	VALIDATION_ERROR("EWBV0001", "InvalidInputData"),
	INVALID_FILE_CONTENT("EWBV0002", "InvalidFileContent");

	private String errorCode;

	private ProcessExceptionCodes(String errorCode, String errorDescription) {
		this.errorCode = errorCode;
	}

	@Override
	public String toString() {
		return this.errorCode;
	}

	public String getErrorCode() {
		return errorCode;
	}

}
