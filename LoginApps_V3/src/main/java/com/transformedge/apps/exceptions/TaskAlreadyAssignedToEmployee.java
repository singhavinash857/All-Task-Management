package com.transformedge.apps.exceptions;

public class TaskAlreadyAssignedToEmployee extends RuntimeException{
	private static final long serialVersionUID = 5861310537366287163L;

	public TaskAlreadyAssignedToEmployee(String string) {
		super(string);
	}
}
