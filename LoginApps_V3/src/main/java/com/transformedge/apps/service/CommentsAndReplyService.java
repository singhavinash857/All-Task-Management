package com.transformedge.apps.service;

import java.util.List;

import javax.validation.Valid;

import com.transformedge.apps.entity.CommentsAndReply;
import com.transformedge.apps.model.CommentsAndReplyResponse;
import com.transformedge.apps.model.CommentsReplyModel;

public interface CommentsAndReplyService {
	CommentsAndReply saveCommentAndReply(@Valid CommentsReplyModel commentsReplyModel, String taskId,
			String commentId);
	CommentsAndReplyResponse getCommentAndReply(String taskId, int pageNum, int pageSize);
	List<CommentsAndReply> getReplyForTaskAndComment(String taskId, String commentId);

}
