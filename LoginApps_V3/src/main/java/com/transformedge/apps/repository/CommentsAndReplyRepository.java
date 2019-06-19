package com.transformedge.apps.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import com.transformedge.apps.entity.CommentsAndReply;

public interface CommentsAndReplyRepository extends CrudRepository<CommentsAndReply, Long>{
	Page<CommentsAndReply> findAll(Pageable pageable);
	CommentsAndReply findByTaskIdAndCommentId(String taskId, String parentId);
	Page<CommentsAndReply> findByTaskId(String taskId, Pageable pageable);
	Page<CommentsAndReply> findByParentIdAndTaskId(String parentId, String taskId, Pageable pageable);
	List<CommentsAndReply> findByTaskIdAndParentId(String taskId,String parentId);
}
