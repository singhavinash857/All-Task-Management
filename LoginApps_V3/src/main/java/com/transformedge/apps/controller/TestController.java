package com.transformedge.apps.controller;

import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.transformedge.apps.appconfiguration.CsvConfiguration;
import com.transformedge.apps.appconfiguration.Translator;
import com.transformedge.apps.entity.Employee;
import com.transformedge.apps.entity.Task;
import com.transformedge.apps.loginlogouthandler.ActiveUserStore;
import com.transformedge.apps.repository.EmployeeRepository;
import com.transformedge.apps.repository.TaskRepository;

@RestController
@RequestMapping(value="${spring.data.rest.base-path}/test_controller")
public class TestController {

	@Autowired
	CsvConfiguration csvConfiguration;

	@Autowired
	MessageSource localeMessageSource;
	
	@Autowired
	TaskRepository taskRespo;

	@GetMapping(value="/test")
	public String testApi(){
		return "Hello World All !!!!";
	}

	@GetMapping(value="")
	public String getMessage(@RequestParam("msg") String msg) {
		return Translator.toLocale(msg);
	}
	
	@GetMapping("/all")
    public String hello() {
        return "Hello Youtube";
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @GetMapping("/secured/all")
    public String securedHello() {
        return "Secured Hello";
    }

    @GetMapping("/secured/alternate")
    public String alternate() {
        return "alternate";
    }
    
    @Autowired
    EmployeeRepository repo;
    
   /* @GetMapping("/test_employee_bymail")
    public Employee getEmployeebyMail() {
        return repo.findByEmployeeMailId("singhavinash5577@transformedge.com");
    }
    */
    
    @GetMapping("/test_task_get")
    public Task testtask() {
    	return null;
     //   return taskRespo.findByTaskSupervisor("singhavinash5577@transformedge.com");
    }
    
}
