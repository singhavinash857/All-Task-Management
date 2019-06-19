package com.transformedge.apps.service;

import java.util.List;

import com.transformedge.apps.entity.Task;
import com.transformedge.apps.model.TaskResponse;

public interface TaskService {
	Task saveTaskDetails(final Task task);
	List<Task> getAllTask();
	Task updateTaskByTaskId(final Task task, long id);
	Task getTaskByTaskId(long id);
	void deleteTaskByTaskId(long id);
	List<Task> getTaskBySupervisorMailId(String mailId);
	Task saveCurrentTaskWithComments(Task taskWithId);
	Task getTaskByTaskId(String name, long id);
	void deleteTaskBySupervisorAndTaskId(String name, long id);
	TaskResponse getAllTaskAssignedByAssigner(String name, int page_number, int pageSize);
	TaskResponse getTaskBySupervisorMailIdByPageNumber(String name, int page_number, int pageSize);
}
