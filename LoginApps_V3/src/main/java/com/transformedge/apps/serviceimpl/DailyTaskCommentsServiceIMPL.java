package com.transformedge.apps.serviceimpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.transformedge.apps.entity.Task;
import com.transformedge.apps.entity.TaskDailyComments;
import com.transformedge.apps.model.TaskDailyCommentsModel;
import com.transformedge.apps.repository.DailyTaskCommentRepository;
import com.transformedge.apps.service.DailyTaskCommentsService;
import com.transformedge.apps.service.TaskService;
import com.transformedge.apps.utils.DateTimeUtility;

@Service
public class DailyTaskCommentsServiceIMPL implements DailyTaskCommentsService{

	@Autowired
	TaskService taskService;
	
	@Autowired
	DailyTaskCommentRepository  dailyTaskCommentRepository;
	
	@Autowired
	DateTimeUtility dateTimeUtility;
	
	@Override
	public TaskDailyComments saveDailyCommentsToTaskBySupervisor(final TaskDailyCommentsModel taskDailyCommentsModel,
			long taskId) {
		Task taskWithId = taskService.getTaskByTaskId(taskId);
		TaskDailyComments taskDailyComments = null;
		if(taskWithId != null){
			taskWithId.setTaskStatus(taskDailyCommentsModel.getTaskStatus());
			taskDailyComments = new TaskDailyComments();
			taskDailyComments.setTaskAssignedBy(taskDailyCommentsModel.getTaskAssignedBy());
			taskDailyComments.setTaskCommentedBy(taskDailyCommentsModel.getTaskSupervisor());
			taskDailyComments.setTaskStatus(taskDailyCommentsModel.getTaskStatus());
			taskDailyComments.setTaskWorkingHours(taskDailyCommentsModel.getTaskWorkingHours());
			taskDailyComments.setTaskComments(taskDailyCommentsModel.getTaskDailyComments());
			taskDailyComments.setTaskCommentDate(dateTimeUtility.getTodayDate());
			taskWithId.getTaskDailyComments().add(taskDailyComments);
			Task savedTask = taskService.saveCurrentTaskWithComments(taskWithId);
			if(savedTask != null){
				return taskDailyComments;
			}
		}
		return null;
	}

	@Override
	public void deleteDailyCommentsByCommentId(long commentId) {
		dailyTaskCommentRepository.deleteById(commentId);		
	}

}
