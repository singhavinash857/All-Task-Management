package com.transformedge.apps.exceptions;

public class UserAlreadyExistException extends RuntimeException{
	private static final long serialVersionUID = 5861310537366287163L;

	public UserAlreadyExistException(String string) {
		super(string);
	}
}
