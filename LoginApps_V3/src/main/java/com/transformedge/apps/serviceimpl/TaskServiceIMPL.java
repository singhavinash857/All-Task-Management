package com.transformedge.apps.serviceimpl;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.mail.internet.AddressException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.transformedge.apps.appconfiguration.Translator;
import com.transformedge.apps.entity.Employee;
import com.transformedge.apps.entity.Task;
import com.transformedge.apps.exceptions.UserNotFoundException;
import com.transformedge.apps.loggedinfo.IAuthenticationFacade;
import com.transformedge.apps.model.TaskResponse;
import com.transformedge.apps.repository.TaskRepository;
import com.transformedge.apps.service.EmployeeService;
import com.transformedge.apps.service.TaskService;
import com.transformedge.apps.utils.DateTimeUtility;
import com.transformedge.apps.utils.MailsUtils;

@Service
public class TaskServiceIMPL implements TaskService{

	@Autowired 
	TaskRepository  taskRepository;

	@Autowired
	EmployeeService  employeeService;

	@Autowired
	MailsUtils mailsUtils;

	@Autowired
	DateTimeUtility dateTimeUtility;

	@Autowired
	private IAuthenticationFacade authenticationFacade;

	//	@Autowired
	//	EmployeeRepository  employeeRepository;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public Task saveTaskDetails(final Task task) {
		logger.info("INSIDE TaskServiceIMPL START METHOD saveTaskDetails");
		/*Task assignedTask = taskRepository.findByTaskSupervisorAndTaskNameAndTaskTypeAndTaskAssignmentType(task.getTaskSupervisor(),
				task.getTaskName(), task.getTaskType(), task.getTaskAssignmentType());
		if(assignedTask != null){
			String emailExistMessage = Translator.toLocale("task.already.assigned");
			throw new TaskAlreadyAssignedToEmployee(emailExistMessage+" "+task.getTaskSupervisor());
		}*/
		String loggedInEmailId = authenticationFacade.getAuthentication().getName();
		task.setTaskAssigner(loggedInEmailId);
		task.setTaskAssignedDate(dateTimeUtility.getTodayDate());
		Employee employee = employeeService.getEmployeeByMailId(task.getTaskSupervisor());
		employee.getTasks().add(task);
		if(employeeService.saveEmployee(employee) != null){
			ExecutorService executorService = Executors.newFixedThreadPool(10);
			executorService.execute(new Runnable() {
				public void run() {
					try {
						mailsUtils.sendAssignedTaskDetailsToSupervisor(task,employee.getEmployeeFirstName(), employee.getEmployeeMailId(), loggedInEmailId);
					} catch (AddressException e) {
						e.printStackTrace();
					} 
				}
			});
			executorService.shutdown();
			return task;
		}
		return task;
	}

	@Override
	public List<Task> getAllTask() {
		List<Task> taskList = (List<Task>) taskRepository.findAll();
		if(taskList != null){
			return taskList;
		}
		return taskList;
	}

	@Override
	public Task updateTaskByTaskId(final Task task, long id) {
		Task taskWithId = taskRepository.findById(id).get();
		if(taskWithId != null){
			task.setTaskAssigner(authenticationFacade.getAuthentication().getName());
			Employee employee = employeeService.getEmployeeByMailId(task.getTaskSupervisor());
			//Employee employee = employeeRepository.findByEmployeeMailId(task.getTaskSupervisor());
			if(employee != null){
				taskWithId.setTaskId(id);
				taskWithId.setTaskAssigner(task.getTaskAssigner());
				taskWithId.setTaskDescription(task.getTaskDescription());
				taskWithId.setTaskDone(task.isTaskDone());
				taskWithId.setTaskName(task.getTaskName());
				taskWithId.setTaskPriority(task.getTaskPriority());
				taskWithId.setTaskStatus(task.getTaskStatus());
				taskWithId.setTaskSupervisor(task.getTaskSupervisor());
				employee.getTasks().add(taskWithId);
				if(employeeService.saveEmployee(employee) != null){
					return task;
				}
			}
		}else{
			String taskNotExist = Translator.toLocale("task.id.notExist");
			throw new UserNotFoundException(taskNotExist + id);
		}
		return taskWithId;
	}

	@Override
	public Task getTaskByTaskId(long id) {
		Task taskWithId = taskRepository.findById(id).get();
		if(taskWithId != null){
			return taskWithId;
		}else{
			String taskNotExist = Translator.toLocale("task.id.notExist");
			throw new UserNotFoundException(taskNotExist + id);
		}
	}

	@Override
	public void deleteTaskByTaskId(long id) {
		Task taskWithId = taskRepository.findById(id).get();
		if(taskWithId != null){
			Employee employee = employeeService.getEmployeeByMailId(taskWithId.getTaskSupervisor());
			//Employee employee = employeeRepository.findByEmployeeMailId(taskWithId.getTaskSupervisor());
			employee.getTasks().remove(taskWithId);
			Employee removedTaskedEmployee = employeeService.saveEmployee(employee);
			//Employee removedTaskedEmployee = employeeRepository.save(employee);
			if(removedTaskedEmployee != null){
				taskRepository.delete(taskWithId);
			}
		}else{
			String taskNotExist = Translator.toLocale("task.id.notExist");
			throw new UserNotFoundException(taskNotExist + id);
		}
	}

	@Override
	public List<Task> getTaskBySupervisorMailId(String taskSeupervisor) {
		List<Task> taskWithSupervisor =  taskRepository.findByTaskSupervisor(taskSeupervisor);
		if(taskWithSupervisor != null){
			return taskWithSupervisor;
		}else{
			String taskNotExist = Translator.toLocale("task.mailId.notExist");
			throw new UserNotFoundException(taskNotExist + taskSeupervisor);
		}
	}

	@Override
	public Task saveCurrentTaskWithComments(Task taskWithId) {
		return taskRepository.save(taskWithId);
	}

	@Override
	public Task getTaskByTaskId(String taskSeupervisor, long taskId) {
		Employee employee = employeeService.getEmployeeByMailId(taskSeupervisor);
		Task task = null;
		if(employee != null){
			task =  taskRepository.findByTaskSupervisorAndTaskId(taskSeupervisor, taskId);
			task.getEmployee().add(employee);
		}
		return task;
	}

	@Override
	public void deleteTaskBySupervisorAndTaskId(String taskSeupervisor, long id) {
		Task taskWithId = taskRepository.findByTaskSupervisorAndTaskId(taskSeupervisor,id);
		if(taskWithId != null){
			Employee employee = employeeService.getEmployeeByMailId(taskWithId.getTaskSupervisor());
			//Employee employee = employeeRepository.findByEmployeeMailId(taskWithId.getTaskSupervisor());
			employee.getTasks().remove(taskWithId);
			Employee removedTaskedEmployee = employeeService.saveEmployee(employee);
			//Employee removedTaskedEmployee = employeeRepository.save(employee);
			if(removedTaskedEmployee != null){
				taskRepository.delete(taskWithId);
			}
		}else{
			String taskNotExist = Translator.toLocale("task.id.notExist");
			throw new UserNotFoundException(taskNotExist + id);
		}
	}

	@SuppressWarnings({ "deprecation", "unused" })
	@Override
	public TaskResponse getAllTaskAssignedByAssigner(String assignerMailId, int page_number, int pageSize) {
		Pageable pageable = new PageRequest((page_number-1), pageSize, Sort.by("taskId").descending());
		Page<Task> page =  taskRepository.findByTaskAssigner(assignerMailId,pageable);
		TaskResponse taskResponse = new TaskResponse();
		setPageInfo(taskResponse,page);
		List<Task> taskByAssigner = page.getContent();
		taskResponse.setValues(taskByAssigner);
		if(taskResponse != null){
			return taskResponse;
		}else{
			String taskNotExist = Translator.toLocale("task.mailId.notExist");
			throw new UserNotFoundException(taskNotExist + assignerMailId);
		}
	}

	@SuppressWarnings("unused")
	public TaskResponse getTaskBySupervisorMailIdByPageNumber(String taskSeupervisor, int page_number,int pageSize) {
		@SuppressWarnings("deprecation")
		Pageable pageable = new PageRequest((page_number-1), pageSize, Sort.by("taskId").descending());
		Page<Task> page =  taskRepository.findByTaskSupervisor(taskSeupervisor,pageable);
		TaskResponse taskResponse = new TaskResponse();
		setPageInfo(taskResponse,page);
		List<Task> taskWithSupervisor = page.getContent();
		taskResponse.setValues(taskWithSupervisor);
		if(taskResponse != null){
			return taskResponse;
		}else{
			String taskNotExist = Translator.toLocale("task.mailId.notExist");
			throw new UserNotFoundException(taskNotExist + taskSeupervisor);
		}	
	}
	
	private void setPageInfo(TaskResponse taskResponse, Page<Task> page) {
		taskResponse.setPageNum(page.getNumber());
		taskResponse.setPageSize(page.getSize());
		taskResponse.setToalPages(page.getTotalPages());
		taskResponse.setTotalCounts(page.getTotalElements());
		taskResponse.setHashMore(page.hasNext());		
	}

}
