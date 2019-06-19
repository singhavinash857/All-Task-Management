package com.transformedge.apps.model;

import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class EmployeeModel {
	
	@NotNull(message = "{user.firstName.notNull}")
	@Size(min = 2, max = 60, message = "{user.firstName.size}")
	private String employeeFirstName;
	
	@Size(min=2,  max = 60, message="{user.lastName.size}")
	private String employeeLastName;
	
	private String employeeGender;
	
	@NotBlank(message = "{user.emailId.notNull}")
	private String employeeMailId;
	
	@NotBlank(message = "{user.empCode.notNull}")
	private String employeeCode;
	
	private String employeePhone;
	
	private String employeeDesignation;
	private String employeeStatus;
	
	@NotNull(message = "{user.password.notNull}")
	@Size(min = 2, max = 60, message = "{user.password.size}")
	private String employeePassword;
	
	@NotNull(message = "{user.confirmpassword.notNull}")
	@Size(min = 2, max = 60, message = "{user.confirmpassword.size}")
	private String employeeConfirmPassword;
	
	private List<String> employeeRole;
	
}
