package com.transformedge.apps.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.transformedge.apps.appconfiguration.Translator;
import com.transformedge.apps.entity.CommentsAndReply;
import com.transformedge.apps.entity.CommentsAndReplyTo;
import com.transformedge.apps.exceptions.ErrorFormInfo;
import com.transformedge.apps.model.CommentsAndReplyResponse;
import com.transformedge.apps.model.CommentsReplyModel;
import com.transformedge.apps.service.CommentsAndReplyService;

@RestController
@RequestMapping(value="${spring.data.rest.base-path}/comments_reply_controller")
public class CommentsAndReplyController {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private CommentsAndReplyService commentsAndReplyService;

	@PostMapping("/comments")
	public ResponseEntity<?> saveCommentsForTask(
			@RequestBody @Valid CommentsReplyModel commentsReplyModel,
			@RequestParam(required = true) String taskId,
			@RequestParam(required = false,defaultValue="") String commentId,
			HttpServletRequest request){
		logger.info("saving the comments by user..");
		CommentsAndReply commentsAndReply = commentsAndReplyService.saveCommentAndReply(commentsReplyModel,taskId,commentId);
		ErrorFormInfo errorInfo = null;
		if (commentsAndReply != null) {
			String successMsg = Translator.toLocale("commentAndReply.added.successfully");
			errorInfo = new ErrorFormInfo(HttpStatus.OK, true, request.getRequestURI(), successMsg, null);
			return new ResponseEntity<>(errorInfo, HttpStatus.OK);
		}
		return new ResponseEntity<>(errorInfo, HttpStatus.RESET_CONTENT);
	}
	
	@GetMapping("/get_comments")
	public ResponseEntity<?> getCommentsForTask(
			@RequestParam(required = false,defaultValue="1") int pageNum,
			@RequestParam(required = false,defaultValue="10") int pageSize,
			@RequestParam(required = true) String taskId,
			HttpServletRequest request){
		logger.info("get all the comments by user..");
		CommentsAndReplyResponse commentsAndReplyResp = commentsAndReplyService.getCommentAndReply(taskId,pageNum,pageSize);
		ErrorFormInfo errorInfo = null;
		if(commentsAndReplyResp != null){
			return new ResponseEntity<>(commentsAndReplyResp, HttpStatus.OK);
		}else{
			String successMsg = Translator.toLocale("commentAndReply.comments.notExist");
			errorInfo = new ErrorFormInfo(HttpStatus.NOT_FOUND, true, request.getRequestURI(), successMsg, null);
		}
		return new ResponseEntity<>(errorInfo, HttpStatus.NOT_FOUND);
	}
	
	
	
	@GetMapping("/comments/replies")
	public ResponseEntity<?> getReplyForTaskAndComment(
			@RequestParam(required = true) String taskId,
			@RequestParam(required = true) String parentId,
			HttpServletRequest request){
		List<CommentsAndReply> commentsAndReplyToResp = commentsAndReplyService.getReplyForTaskAndComment(taskId,parentId);
		if(commentsAndReplyToResp != null){
			return new ResponseEntity<>(commentsAndReplyToResp, HttpStatus.OK);
		}
		return new ResponseEntity<>(commentsAndReplyToResp, HttpStatus.NOT_FOUND);
	}
	
}
