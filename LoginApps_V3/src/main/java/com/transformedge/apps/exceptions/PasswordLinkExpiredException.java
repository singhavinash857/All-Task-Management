package com.transformedge.apps.exceptions;

public class PasswordLinkExpiredException extends RuntimeException{
	private static final long serialVersionUID = 5861310537366287163L;

	public PasswordLinkExpiredException(String string) {
		super(string);
	}
}
