package com.transformedge.apps.exceptions;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;

import lombok.Data;

@Data
public class ErrorFormInfo {
    
    private HttpStatus status;
    private boolean success;
    private String url;
    private String message;
    private List< FieldErrorDTO > errors = new ArrayList< FieldErrorDTO >();
     
    public ErrorFormInfo() {}
     
    public ErrorFormInfo(String url, String message) {
            this.url = url;
            this.message = message;
    }

	public ErrorFormInfo(HttpStatus status, boolean success, String url, String message, List<FieldErrorDTO> errors) {
		super();
		this.status = status;
		this.success = success;
		this.url = url;
		this.message = message;
		this.errors = errors;
	}
     
	public ErrorFormInfo(HttpStatus badRequest, String errorURL, String errorMessage) {
		super();
		this.status = badRequest;
		this.url = errorURL;
		this.message = errorMessage;
	}
	
}
