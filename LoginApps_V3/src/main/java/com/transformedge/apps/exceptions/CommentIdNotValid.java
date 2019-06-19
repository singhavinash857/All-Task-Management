package com.transformedge.apps.exceptions;

public class CommentIdNotValid  extends RuntimeException{
	
	private static final long serialVersionUID = 5491699366707218449L;

	public CommentIdNotValid(String string) {
		super(string);
	}
}
