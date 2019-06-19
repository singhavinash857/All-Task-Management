package com.transformedge.apps.exceptions;

import lombok.Data;

@Data
public class FieldErrorDTO {
    
    private String fieldName;
    private String fieldError;
    private String errorDescription;
     
    public FieldErrorDTO(String fieldName, String fieldError) {
            this.fieldName = fieldName;
            this.fieldError = fieldError;
    }

	public FieldErrorDTO(String fieldName, String fieldError, String errorDescription) {
		super();
		this.fieldName = fieldName;
		this.fieldError = fieldError;
		this.errorDescription = errorDescription;
	}

	public FieldErrorDTO(String errorDescription) {
		super();
		this.errorDescription = errorDescription;
	}
	
}
