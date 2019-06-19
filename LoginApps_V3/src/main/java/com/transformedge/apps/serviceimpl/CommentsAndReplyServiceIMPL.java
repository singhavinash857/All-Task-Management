package com.transformedge.apps.serviceimpl;

import java.util.List;
import java.util.UUID;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.transformedge.apps.appconfiguration.Translator;
import com.transformedge.apps.entity.CommentsAndReply;
import com.transformedge.apps.entity.CommentsAndReplyTo;
import com.transformedge.apps.exceptions.CommentIdNotValid;
import com.transformedge.apps.exceptions.PageSizeExceedException;
import com.transformedge.apps.loggedinfo.IAuthenticationFacade;
import com.transformedge.apps.model.CommentsAndReplyResponse;
import com.transformedge.apps.model.CommentsReplyModel;
import com.transformedge.apps.repository.CommentsAndReplyRepository;
import com.transformedge.apps.repository.CommentsAndReplyRepositoryTo;
import com.transformedge.apps.service.CommentsAndReplyService;
import com.transformedge.apps.utils.DateTimeUtility;

@Service
public class CommentsAndReplyServiceIMPL implements CommentsAndReplyService{

	@Autowired
	private CommentsAndReplyRepository commentsAndReplyRepository;


	@Autowired
	DateTimeUtility dateTimeUtility;

	@Autowired
	private IAuthenticationFacade authenticationFacade;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());


	@Override
	public CommentsAndReply saveCommentAndReply(@Valid CommentsReplyModel commentsReplyModel, String taskId,
			String commentId) {
		logger.info("calling service for saving : "+commentsReplyModel);
		CommentsAndReply  commentsAndReply = null;

		/*
		 *  testing only..
		 */
		String commentedBy = "avinash.singh@transformedge.com";
	 // String commentedBy = authenticationFacade.getAuthentication().getName();

		if(commentId != null && commentId.trim().length() != 0){
			CommentsAndReply commentsAndReplyTo = commentsAndReplyRepository.findByTaskIdAndCommentId(taskId, commentId);
			if(commentsAndReplyTo != null){
				commentsAndReplyTo.setHashReply(true);
				commentsAndReplyRepository.save(commentsAndReplyTo);
			}else{
				String commentIdNotValid = Translator.toLocale("commentAndReply.commentId.invalid");
				throw new CommentIdNotValid(commentIdNotValid);
			}
			commentsAndReply = new CommentsAndReply();
			commentsAndReply.setCommentId(UUID.randomUUID().toString().toUpperCase());
			commentsAndReply.setComment(commentsReplyModel.getComment());
			commentsAndReply.setCommentDate(dateTimeUtility.getTodayDate());
			commentsAndReply.setCommentTime(dateTimeUtility.getCurrentTime());
			commentsAndReply.setTaskId(taskId);
			commentsAndReply.setParentId(commentId);
			commentsAndReply.setCommentedBy(commentedBy);
			commentsAndReplyRepository.save(commentsAndReply);
		}else{
			commentsAndReply = new CommentsAndReply();
			commentsAndReply.setCommentId(UUID.randomUUID().toString().toUpperCase());
			commentsAndReply.setComment(commentsReplyModel.getComment());
			commentsAndReply.setCommentDate(dateTimeUtility.getTodayDate());
			commentsAndReply.setCommentTime(dateTimeUtility.getCurrentTime());
			commentsAndReply.setTaskId(taskId);
			commentsAndReply.setCommentedBy(commentedBy);
			commentsAndReplyRepository.save(commentsAndReply);
		}
		return commentsAndReply;
	}


	@SuppressWarnings("deprecation")
	@Override
	public CommentsAndReplyResponse getCommentAndReply(String taskId, int pageNum, int pageSize) {
		final String parentId = null;
		Pageable pageable = new PageRequest((pageNum-1), pageSize, Sort.by("commentId").ascending());
		CommentsAndReplyResponse response = new CommentsAndReplyResponse();
		if(pageSize > 1000){
			String pageSizeExceedMessage = Translator.toLocale("commentAndReply.pageSize.exceed");
			throw new PageSizeExceedException(pageSizeExceedMessage);
		}else if(pageNum > 0 && pageSize > 0){
			Page<CommentsAndReply> page = commentsAndReplyRepository.findByParentIdAndTaskId(parentId,taskId,pageable);
			response.setPageNum(page.getNumber());
			response.setPageSize(page.getSize());
			response.setToalPages(page.getTotalPages());
			response.setTotalCounts(page.getTotalElements());
			response.setHashMore(page.hasNext());
			List<CommentsAndReply>  list = page.getContent();
			response.setValues(list);
		}else{
			String pageSizeInvalidMessage = Translator.toLocale("commentAndReply.pageSize.invalid");
			throw new PageSizeExceedException(pageSizeInvalidMessage);
		}
		return response;
	}


	@Override
	public List<CommentsAndReply> getReplyForTaskAndComment(String taskId, String commentId) {
		List<CommentsAndReply> list = commentsAndReplyRepository.findByTaskIdAndParentId(taskId, commentId);
		return list;
	}

}
