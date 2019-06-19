package com.transformedge.apps.controller;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.transformedge.apps.appconfiguration.Translator;
import com.transformedge.apps.entity.TaskDailyComments;
import com.transformedge.apps.exceptions.ErrorFormInfo;
import com.transformedge.apps.model.TaskDailyCommentsModel;
import com.transformedge.apps.service.DailyTaskCommentsService;

@RestController
@CrossOrigin
@RequestMapping(value="${spring.data.rest.base-path}/comments_controller")
public class DailyCommentsController {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired 
	DailyTaskCommentsService dailyTaskCommentsService;

	@PostMapping(value="save_daily_comments/{taskId}")
	public ResponseEntity<?> saveDailyCommentsToTaskBySupervisor(@RequestBody @Valid TaskDailyCommentsModel taskDailyCommentsModel, @PathVariable long taskId, HttpServletRequest request){
		logger.info("INSIDE DailyCommentsController START METHOD saveDailyCommentsToTaskBySupervisor:");
		final TaskDailyComments taskDailyComments = dailyTaskCommentsService.saveDailyCommentsToTaskBySupervisor(taskDailyCommentsModel,taskId);
		ErrorFormInfo errorInfo = null;
		if (taskDailyComments != null) {
			String successMsg = Translator.toLocale("comments.added.successfully");
			errorInfo = new ErrorFormInfo(HttpStatus.OK, true, request.getRequestURI(), successMsg, null);
			return new ResponseEntity<>(errorInfo, HttpStatus.OK);
		}
		return new ResponseEntity<>(errorInfo, HttpStatus.RESET_CONTENT);
	}

	@DeleteMapping(value="delete_comments_by_id/{commentId}")
	public ResponseEntity<?> deleteDailyCommentsByCommentId(@PathVariable long commentId, HttpServletRequest request){
		dailyTaskCommentsService.deleteDailyCommentsByCommentId(commentId);
		String successMsg = Translator.toLocale("comments.deleted.successfully");
		ErrorFormInfo errorInfo = new ErrorFormInfo(HttpStatus.OK, true, request.getRequestURI(), successMsg, null);
		return new ResponseEntity<>(errorInfo, HttpStatus.OK);
	}
	
}
