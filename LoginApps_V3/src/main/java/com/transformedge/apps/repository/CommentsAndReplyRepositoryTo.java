package com.transformedge.apps.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.transformedge.apps.entity.CommentsAndReplyTo;

public interface CommentsAndReplyRepositoryTo extends CrudRepository<CommentsAndReplyTo, String>{
	List<CommentsAndReplyTo> findByTaskIdAndCommentId(String taskId, String commentId);
}
