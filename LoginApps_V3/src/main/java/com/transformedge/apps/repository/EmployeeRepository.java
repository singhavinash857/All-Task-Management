package com.transformedge.apps.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.transformedge.apps.entity.Employee;

public interface EmployeeRepository extends CrudRepository<Employee, Long>{
		Employee findByEmployeeMailId(String employeemailid);
		@Query("SELECT u FROM Employee u WHERE u.employeeStatus = 'Active'")
		Page<Employee> findAllEmployees(Pageable pageable);
		List<Employee> findAll(Pageable pageable);
		
		
}
