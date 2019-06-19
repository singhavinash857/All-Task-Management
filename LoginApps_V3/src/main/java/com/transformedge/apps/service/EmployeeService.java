package com.transformedge.apps.service;

import java.util.List;

import javax.validation.Valid;

import com.transformedge.apps.entity.Employee;
import com.transformedge.apps.entity.Role;
import com.transformedge.apps.model.EmployeeModel;
import com.transformedge.apps.model.EmployeeResponse;

public interface EmployeeService {
	Employee registerEmployee(@Valid EmployeeModel employeeModel);
	EmployeeResponse getAllRegisteredEmployee(int pageNum, int pageSize);
	Employee getEmployeeById(long id);
	Employee updateEmployee(EmployeeModel employeeModel, long id);
	Employee getEmployeeByMailId(String name);
	Employee saveEmployee(Employee employee);
	EmployeeResponse searchEmployeeByCondition(String mailId, String projectName, int page_number, int pageSize);
	Employee deleteRoleByEmployeeId(String roleName, long employeeId);
	List<Role> getEmployeeRolesById(long id);
}
