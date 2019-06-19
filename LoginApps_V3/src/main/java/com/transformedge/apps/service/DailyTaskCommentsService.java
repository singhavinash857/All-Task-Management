package com.transformedge.apps.service;

import com.transformedge.apps.entity.TaskDailyComments;
import com.transformedge.apps.model.TaskDailyCommentsModel;

public interface DailyTaskCommentsService {
	TaskDailyComments saveDailyCommentsToTaskBySupervisor(final TaskDailyCommentsModel taskDailyCommentsModel, long taskId);
	void deleteDailyCommentsByCommentId(long commentId);
}
