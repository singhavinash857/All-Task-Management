package com.transformedge.apps.repository;

import org.springframework.data.repository.CrudRepository;

import com.transformedge.apps.entity.TaskDailyComments;

public interface DailyTaskCommentRepository extends CrudRepository<TaskDailyComments, Long>{

}
