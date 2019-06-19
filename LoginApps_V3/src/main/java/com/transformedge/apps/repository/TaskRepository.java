package com.transformedge.apps.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import com.transformedge.apps.entity.Task;
import java.lang.String;

public interface TaskRepository extends CrudRepository<Task, Long>{
	Task findByTaskNameAndTaskSupervisor(String taksName, String taskSupervisor);
	List<Task> findByTaskSupervisor(String taskSeupervisor);
	Task findByTaskSupervisorAndTaskId(String taskSeupervisor , long taskId);
	Page<Task> findByTaskAssigner(String taskassigner, Pageable pageable);
	Page<Task> findByTaskSupervisor(String taskSeupervisor, Pageable pageable);
	Task findByTaskSupervisorAndTaskNameAndTaskTypeAndTaskAssignmentType(String supervisor,String taskName,String taskType,String taskAssignmentType);
}
