package com.transformedge.apps.serviceimpl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.transformedge.apps.appconfiguration.Translator;
import com.transformedge.apps.entity.Employee;
import com.transformedge.apps.entity.Role;
import com.transformedge.apps.entity.Task;
import com.transformedge.apps.entity.User;
import com.transformedge.apps.exceptions.UserAlreadyExistException;
import com.transformedge.apps.exceptions.UserNotFoundException;
import com.transformedge.apps.model.EmployeeModel;
import com.transformedge.apps.model.EmployeeResponse;
import com.transformedge.apps.repository.EmployeeRepository;
import com.transformedge.apps.repository.RoleRepository;
import com.transformedge.apps.repository.UserRepository;
import com.transformedge.apps.service.EmployeeService;
import com.transformedge.apps.utils.EncryptDecryptPassword;

@Service
public class EmployeeServiceIMPL implements EmployeeService{

	@Autowired
	EmployeeRepository  employeeRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	RoleRepository roleRepository;

	@Autowired
	EncryptDecryptPassword encryptDecryptPassword;

	BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

	public Employee registerEmployee(final EmployeeModel employeeModel) {
		if (emailExists(employeeModel.getEmployeeMailId())) {
			String emailExistMessage = Translator.toLocale("user.email.exist");
			throw new UserAlreadyExistException(emailExistMessage + employeeModel.getEmployeeMailId());
		}
		final Employee employee = new Employee();
		final User user = new User();
		try {
			user.setUsername(employeeModel.getEmployeeMailId());
			user.setPassword(bCryptPasswordEncoder.encode((employeeModel.getEmployeePassword())));
			user.setPasswordConfirm(bCryptPasswordEncoder.encode((employeeModel.getEmployeeConfirmPassword())));
			for(String roles : employeeModel.getEmployeeRole()){
				Role userRole = roleRepository.findByName(roles);
				if(userRole != null){
					user.getRoles().add(userRole);
				}else{
					user.getRoles().add(new Role(roles));
				}
			}
			if(userRepository.save(user) != null){
				employee.setEmployeeCode(employeeModel.getEmployeeCode());
				employee.setEmployeeFirstName(employeeModel.getEmployeeFirstName());
				employee.setEmployeeLastName(employeeModel.getEmployeeLastName());
				employee.setEmployeeGender(employeeModel.getEmployeeGender());
				employee.setEmployeeDesignation(employeeModel.getEmployeeDesignation());
				employee.setEmployeeMailId(employeeModel.getEmployeeMailId());
				employee.setEmployeePhone(employeeModel.getEmployeePhone());
				employee.setEmployeeStatus(employeeModel.getEmployeeStatus());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return employeeRepository.save(employee);
	}

	private boolean emailExists(final String employeemailid) {
		return employeeRepository.findByEmployeeMailId(employeemailid) != null;
	}

	@SuppressWarnings("deprecation")
	public EmployeeResponse getAllRegisteredEmployee(int pageNum,int pageSize) {
		Pageable pageable = new PageRequest((pageNum-1), pageSize, Sort.by("employeeId").descending());
		Page<Employee> page =  employeeRepository.findAllEmployees(pageable);
		List<Employee> employeeList = page.getContent();
		
		EmployeeResponse employeeResponse = new EmployeeResponse();
		employeeResponse.setPageNum(page.getNumber());
		employeeResponse.setPageSize(page.getSize());
		employeeResponse.setToalPages(page.getTotalPages());
		employeeResponse.setTotalCounts(page.getTotalElements());
		employeeResponse.setHashMore(page.hasNext());
		if(employeeList != null){
			for(Employee employee : employeeList){
				if(employee.getEmployeeMailId() != null){
					User user = userRepository.findByUsername(employee.getEmployeeMailId());
					List<Role> roles = user.getRoles();
					employee.setEmployeeRoles(new HashSet<>(roles));
				}
			}
		}
		
		employeeResponse.setValues(employeeList);
		return employeeResponse;
	}

	public Employee getEmployeeById(long id) {
		Employee employee = employeeRepository.findById(id).orElse(null);
		if(employee != null){
			return employee;
		}else{
			String userNotExist = Translator.toLocale("user.id.notExist");
			throw new UserNotFoundException(userNotExist + id);
		}
	}

	public Employee updateEmployee(EmployeeModel employeeModel, long id) {
		Employee employee = employeeRepository.findById(id).orElse(null);
		if(employee != null){
			User user = userRepository.findByUsername(employee.getEmployeeMailId());
			if(user != null){
				if(employeeModel.getEmployeeStatus().equalsIgnoreCase("INACTIVE")){
					user.setEnabled(0);
				}
				user.setUsername(employeeModel.getEmployeeMailId());
				if(employeeModel.getEmployeePassword() != null || employeeModel.getEmployeePassword() != ""){
					user.setPassword(bCryptPasswordEncoder.encode((employeeModel.getEmployeePassword())));
					user.setPasswordConfirm(bCryptPasswordEncoder.encode((employeeModel.getEmployeeConfirmPassword())));
				}
				for(String roles : employeeModel.getEmployeeRole()){
					Role userRole = roleRepository.findByName(roles);
					if(userRole != null){
						if(null == user.getRoles().stream().filter(r -> r.getName().equalsIgnoreCase(userRole.getName())).findAny().orElse(null)){
							user.getRoles().add(userRole);
						}
					}else{
						user.getRoles().add(new Role(roles));
					}
				}
				if(userRepository.save(user) != null){
					employee.setEmployeeId(id);
					employee.setEmployeeCode(employeeModel.getEmployeeCode());
					employee.setEmployeeFirstName(employeeModel.getEmployeeFirstName());
					employee.setEmployeeLastName(employeeModel.getEmployeeLastName());
					employee.setEmployeeGender(employeeModel.getEmployeeGender());
					employee.setEmployeeDesignation(employeeModel.getEmployeeDesignation());
					employee.setEmployeeMailId(employeeModel.getEmployeeMailId());
					employee.setEmployeePhone(employeeModel.getEmployeePhone());
					employee.setEmployeeStatus(employeeModel.getEmployeeStatus());
					return employeeRepository.save(employee);
				}
			}else{
				String userNotExist = Translator.toLocale("user.id.notExist");
				throw new UserNotFoundException(userNotExist + id);
			}
		}
		return employee;
	}

	public Employee getEmployeeByMailId(String mailId) {
		Employee empObj = employeeRepository.findByEmployeeMailId(mailId);
		if(empObj != null){
			return empObj;
		}else{
			String userNotExist = Translator.toLocale("user.maill.notExist");
			throw new UserNotFoundException(userNotExist + mailId);
		}
	}

	@Override
	public Employee saveEmployee(Employee employee) {
		return employeeRepository.save(employee);
	}

	@SuppressWarnings("deprecation")
	@Override
	public EmployeeResponse searchEmployeeByCondition(String mailId, String projectName, int pageNumber, int pageSize) {
		List<Employee> employeeList = new ArrayList<>();
		Pageable pageable = new PageRequest((pageNumber-1), pageSize, Sort.by("employeeId").descending());
		EmployeeResponse employeeResponse = new EmployeeResponse();
		if(mailId.trim().length() != 0 && projectName.trim().length() != 0 ){
			Employee employee = employeeRepository.findByEmployeeMailId(mailId);
			if(employee != null){
				List<Task> list = employee.getTasks().stream().filter(p -> p.getTaskName().equalsIgnoreCase(projectName)).collect(Collectors.toList());;
				employee.getTasks().retainAll(list);
				employeeList.add(employee);
				employeeResponse.setValues(employeeList);
			}
		}else if(mailId.trim().length() != 0){
			Employee employee = employeeRepository.findByEmployeeMailId(mailId);
			employeeList.add(employee);
			employeeResponse.setValues(employeeList);
		}else if(projectName.trim().length() != 0){
			Page<Employee> page = employeeRepository.findAllEmployees(pageable);
			List<Employee> employeeList1 = page.getContent();
			employeeResponse.setPageNum(page.getNumber());
			employeeResponse.setPageSize(page.getSize());
			employeeResponse.setToalPages(page.getTotalPages());
			employeeResponse.setTotalCounts(page.getTotalElements());
			employeeResponse.setHashMore(page.hasNext());
			employeeResponse.setValues(employeeList1);
			//List<Employee> employeeList1 = (List<Employee>) employeeRepository.findAllEmployees(pageable).getContent();
			if(employeeList1 != null){
				for(Employee employee : employeeList1){
					List<Task> list = employee.getTasks().stream().filter(p -> p.getTaskName().equalsIgnoreCase(projectName)).collect(Collectors.toList());;
					employee.getTasks().retainAll(list);
					employeeList.add(employee);
				}
			}
		}else{
			Page<Employee> page = employeeRepository.findAllEmployees(pageable);
			List<Employee> totalEmployees = page.getContent();
			employeeResponse.setPageNum(page.getNumber());
			employeeResponse.setPageSize(page.getSize());
			employeeResponse.setToalPages(page.getTotalPages());
			employeeResponse.setTotalCounts(page.getTotalElements());
			employeeResponse.setHashMore(page.hasNext());
			employeeResponse.setValues(totalEmployees);
			return employeeResponse;
			//return (List<Employee>) employeeRepository.findAllEmployees(pageable).getContent();
		}
		return employeeResponse;
	}

	@Override
	public Employee deleteRoleByEmployeeId(String roleName, long employeeId) {
		Employee employee = employeeRepository.findById(employeeId).orElse(null);
		if(employee != null){
			User user = userRepository.findByUsername(employee.getEmployeeMailId());
			if(user != null){
				System.out.println("user.getRoles()"+user.getRoles());
				Role role = roleRepository.findByName(roleName);
				if(role != null){
					user.getRoles().remove(role);
				}if(userRepository.save(user) != null){
					if(employeeRepository.save(employee) != null){
						return employee;
					}
				}
			}
		}	
		return employee;
	}

	@Override
	public List<Role> getEmployeeRolesById(long employeeId) {
		Employee employee = employeeRepository.findById(employeeId).orElse(null);
		if(employee != null){
			User user = userRepository.findByUsername(employee.getEmployeeMailId());
			if(user != null){
				return user.getRoles();
			}
		}
		return null;
	}
}
