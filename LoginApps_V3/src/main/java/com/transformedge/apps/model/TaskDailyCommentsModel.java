package com.transformedge.apps.model;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class TaskDailyCommentsModel {
	@NotNull(message = "{dailyComment.taskAssignedBy.notNull}")
	private String taskAssignedBy;
	@NotNull(message = "{dailyComment.taskSupervisor.notNull}")
	private String taskSupervisor;
	private String taskStatus;
	@NotNull(message = "{dailyComment.workingHours.notNull}")
	private String taskWorkingHours;
	@NotBlank(message = "{dailyComment.comment.notNull}")
	private String taskDailyComments;
	
}
