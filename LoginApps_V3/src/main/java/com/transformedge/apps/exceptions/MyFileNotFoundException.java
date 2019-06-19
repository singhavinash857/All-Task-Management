package com.transformedge.apps.exceptions;

public class MyFileNotFoundException extends RuntimeException{
	private static final long serialVersionUID = 1L;

	public MyFileNotFoundException(String string) {
		super(string);
	}
}
