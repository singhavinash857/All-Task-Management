package com.transformedge.apps.exceptions;

public class PageSizeExceedException extends RuntimeException{
	private static final long serialVersionUID = 4248626881565350110L;

	public PageSizeExceedException(String string) {
		super(string);
	}
}
