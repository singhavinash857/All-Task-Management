package com.transformedge.apps.controller;

import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.transformedge.apps.appconfiguration.Translator;
import com.transformedge.apps.entity.Task;
import com.transformedge.apps.exceptions.ErrorFormInfo;
import com.transformedge.apps.loggedinfo.IAuthenticationFacade;
import com.transformedge.apps.model.TaskResponse;
import com.transformedge.apps.service.EmployeeService;
import com.transformedge.apps.service.TaskService;

@CrossOrigin
@RestController
@RequestMapping(value="${spring.data.rest.base-path}/task_controller")
public class TaskController {

	@Autowired
	TaskService taskService;

	@Autowired
	EmployeeService employeeServiceIMPL;
	
	@Autowired
	private IAuthenticationFacade authenticationFacade;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@PostMapping(value="/save_task")
	public ResponseEntity<?> saveTaskForEmployee(@RequestBody @Valid Task task,HttpServletRequest request){
		logger.info("INSIDE TaskController START METHOD saveTaskForEmployee");
		final Task registeredTask = taskService.saveTaskDetails(task);
		ErrorFormInfo errorInfo = null;
		if (registeredTask != null) {
			String successMsg = Translator.toLocale("task.added.successfully");
			errorInfo = new ErrorFormInfo(HttpStatus.OK, true, request.getRequestURI(), successMsg, null);
			return new ResponseEntity<>(errorInfo, HttpStatus.OK);
		}
		return new ResponseEntity<>(errorInfo, HttpStatus.RESET_CONTENT);
	}

	@GetMapping(value="/get_all_task")
	public ResponseEntity<?> getAllTask(){
		logger.info("INSIDE TaskController START METHOD getAllTask");
		List<Task> taskList = taskService.getAllTask();
		if(taskList != null){
			Collections.reverse(taskList);
			return new ResponseEntity<>(taskList,HttpStatus.OK);
		}
		return new ResponseEntity<>(taskList,HttpStatus.NOT_FOUND);
	}
	
	@GetMapping(value="/get_all_task_by_Assigner")
	public ResponseEntity<?> getAllTaskAssignedByAssigner(
			@RequestParam(required = false,defaultValue="1") int page_number,
			@RequestParam(required = false,defaultValue="10") int pageSize,
			HttpServletRequest request){
		logger.info("INSIDE TaskController START METHOD getAllTask");
		TaskResponse TaskResponse = taskService.getAllTaskAssignedByAssigner(authenticationFacade.getAuthentication().getName(),page_number,pageSize);
		ErrorFormInfo errorInfo = null;
		if(TaskResponse != null){
			return new ResponseEntity<>(TaskResponse,HttpStatus.OK);
		}else{
			String successMsg = Translator.toLocale("user.list.notExist");
			errorInfo = new ErrorFormInfo(HttpStatus.NOT_FOUND, true, request.getRequestURI(), successMsg, null);
		}
		return new ResponseEntity<>(errorInfo, HttpStatus.NOT_FOUND);
	}

	@PutMapping(value="/update_task/{id}")
	public ResponseEntity<?> updateTaskByTaskId(@RequestBody @Valid Task task, @PathVariable long id,HttpServletRequest request){
		logger.info("INSIDE TaskController START METHOD updateTaskByTaskId");
		final Task updatedTask = taskService.updateTaskByTaskId(task, id);
		ErrorFormInfo errorInfo = null;
		if (updatedTask != null) {
			String successMsg = Translator.toLocale("user.updated.successfully");
			errorInfo = new ErrorFormInfo(HttpStatus.ACCEPTED, true, request.getRequestURI(), successMsg, null);
			return new ResponseEntity<>(errorInfo, HttpStatus.ACCEPTED);
		}
		return new ResponseEntity<>(errorInfo, HttpStatus.NOT_MODIFIED);
	}


	@GetMapping(value="get_task/{id}")
	public ResponseEntity<?> getTaskByTaskId(@PathVariable long id){
		logger.info("INSIDE TaskController START METHOD getTaskByTaskId");
		logger.info("task id ::"+id);
		Task taskWithId = taskService.getTaskByTaskId(id);
		/*
	    Task taskWithIdAndLoggedinUser = taskService.getTaskByTaskId(authenticationFacade.getAuthentication().getName(),id);
		if(taskWithIdAndLoggedinUser != null){
			return new ResponseEntity<>(taskWithIdAndLoggedinUser,HttpStatus.FOUND);
		}
		*/
		if(taskWithId != null){
			return new ResponseEntity<>(taskWithId,HttpStatus.OK);
		}
		return new ResponseEntity<>(taskWithId,HttpStatus.NOT_FOUND);
	}

	@DeleteMapping(value="delete_task/{id}")
	public ResponseEntity<?> deleteTaskByTaskId(@PathVariable long id, HttpServletRequest request){
		logger.info("INSIDE TaskController START METHOD deleteTaskByTaskId");
		taskService.deleteTaskByTaskId(id);
		//taskService.deleteTaskBySupervisorAndTaskId(authenticationFacade.getAuthentication().getName(),id);
		
		String successMsg = Translator.toLocale("task.deleted.successfully");
		ErrorFormInfo errorInfo = new ErrorFormInfo(HttpStatus.OK, true, request.getRequestURI(), successMsg, null);
		return new ResponseEntity<>(errorInfo, HttpStatus.OK);
	}
	
	@GetMapping(value="get_task_by_supervisor/{mailId}")
	public ResponseEntity<?> getTaskBySupervisorMailId(@PathVariable String mailId){
		logger.info("INSIDE TaskController START METHOD getTaskBySupervisorMailId");
		List<Task> taskBySupervisor = taskService.getTaskBySupervisorMailId(mailId);
		if(taskBySupervisor != null){
			return new ResponseEntity<>(taskBySupervisor,HttpStatus.OK);
		}
		return new ResponseEntity<>(taskBySupervisor,HttpStatus.NOT_FOUND);
	}
	
	@GetMapping("/get_task_assigned_to_supervisor")
	public ResponseEntity<?> getTaskBySupervisorMailId(
			@RequestParam(required = false,defaultValue="1") int page_number,
			@RequestParam(required = false,defaultValue="10") int pageSize,
			HttpServletRequest request){
		logger.info("INSIDE TaskController START METHOD getTaskBySupervisorMailId");
		TaskResponse taskBySupervisor = taskService.getTaskBySupervisorMailIdByPageNumber(authenticationFacade.getAuthentication().getName(),page_number,pageSize);
		ErrorFormInfo errorInfo = null;
		if(taskBySupervisor != null){
			return new ResponseEntity<>(taskBySupervisor,HttpStatus.OK);
		}else{
			String successMsg = Translator.toLocale("task.list.notExist");
			errorInfo = new ErrorFormInfo(HttpStatus.NOT_FOUND, true, request.getRequestURI(), successMsg, null);
		}
		return new ResponseEntity<>(errorInfo, HttpStatus.NOT_FOUND);
	}
}
